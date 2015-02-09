package com.avmurzin.palaverpassim.controllers

import com.avmurzin.palaverpassim.db.Abonent
import com.avmurzin.palaverpassim.db.Palaver
import com.avmurzin.palaverpassim.db.Conference
import com.avmurzin.palaverpassim.global.AbonentStatus
import com.avmurzin.palaverpassim.global.AudioStatus;
import com.avmurzin.palaverpassim.global.PalaverStatus
import com.avmurzin.palaverpassim.global.ReturnMessage
import com.avmurzin.palaverpassim.global.Mode
import com.avmurzin.palaverpassim.system.CallMachine
import com.avmurzin.palaverpassim.ui.UiManipulation
import com.avmurzin.palaverpassim.system.AsteriskMachine

import java.util.UUID;

class PalaverController {

	private final UiManipulation uiManipulation = UiManipulation.getInstance();
	private final CallMachine callMachine = AsteriskMachine.getInstance();

	def index() {
		redirect(url: "/index.html")
//		render(contentType: "application/json") {
//			result = false
//			message = "Несуществующая операция"
//		}
	}

	def getTimezone() {
		def config = new ConfigSlurper().parse(new File('ConfigSlurper/palaverpassim.conf').toURI().toURL())
		render(contentType: "application/json") {
			timezone = "${config.timezone.shift / 3600}"
		}
	}
	
	def createAbonent() {
		def jsonObject = request.JSON
		UUID uuid = UUID.randomUUID()
		def abonent = new Abonent(jsonObject)
		abonent.uuid = uuid
		abonent.save(failOnError: true, flush: true)
		
		render(contentType: "application/json") {
			result = true
			message = uuid.toString()
		}
	}

	/**
	 * Создать сессию в указанных временных рамках.
	 * -Создатель получает права CONF_OWNER.
	 * -Доступ: ${palaverUuid}:WEBUSER
	 * @return
	 */
	def createPalaver() {
		String description = params.description
		long startTimestamp = params.startTimestamp
		long stopTimestamp = params.stopTimestamp

		UUID uuid = UUID.randomUUID()

		def conference = Conference.findByPhoneNumber("3-63-00")

		def palaver = new Palaver(conference: conference, uuid: uuid, description: description, startTimestamp: startTimestamp, stopTimestamp: stopTimestamp)
		palaver.save(failOnError: true, flush: true)

		render(contentType: "application/json") {
			result = true
			message = uuid.toString()
		}
	}

	/**
	 * Запустить сессию в произвольное время (по команде).
	 * -Доступ: ${palaverUuid}:CONF_OWNER
	 * @return JSON - ReturnMessage
	 */
	def startPalaver() {
		UUID uuid = UUID.fromString(params.uuid);

		ReturnMessage returnMessage = uiManipulation.startPalaver(uuid, Mode.MANUAL);

		render(contentType: "application/json") {
			result = returnMessage.result
			message = returnMessage.message
		}
	}

	/**
	 * Остановить сессию в произвольное время (по команде).
	 * -Доступ: ${palaverUuid}:CONF_OWNER
	 * @return JSON - ReturnMessage
	 */
	def stopPalaver() {
		UUID uuid = UUID.fromString(params.uuid);

		ReturnMessage returnMessage = uiManipulation.stopPalaver(uuid);

		render(contentType: "application/json") {
			result = returnMessage.result
			message = returnMessage.message
		}
	}

	/**
	 * Получить сессию со списком абонентов и их статусами (и статусами 
	 * звуковых потоков).
	 * -Доступ: ${palaverUuid}:WEBUSER
	 * @param uuid
	 * @return JSON
	 */
	def getPalaver() {
		UUID puuid = UUID.fromString(params.uuid);

		AbonentStatus abonentStatus = AbonentStatus.DISCONNECTED;
		AudioStatus audioToStatus = AudioStatus.UNMUTED;
		AudioStatus audioFromStatus = AudioStatus.UNMUTED;
		PalaverStatus palaverStatus = PalaverStatus.INACTIVE;

		Palaver palaver = Palaver.findByUuid(puuid);
		Conference pconference = palaver.conference;
		def abonents = palaver.abonent.findAll();
		
	
		render(contentType: "application/json") {
			uuid = puuid.toString()
			description = palaver.description
			conference = pconference.description
			items = array {
				for(Abonent abonent : abonents) {

					if(callMachine.isAbonentConnected(abonent, pconference)) {
						abonentStatus = AbonentStatus.CONNECTED
					} else {
						abonentStatus = AbonentStatus.DISCONNECTED
					}
					if(callMachine.isMutedFromAbonent(abonent, pconference)) {
						audioFromStatus = AudioStatus.MUTED
					} else {
						audioFromStatus = AudioStatus.UNMUTED
					}
					if(callMachine.isMutedToAbonent(abonent, pconference)) {
						audioToStatus = AudioStatus.MUTED
					} else {
						audioToStatus = AudioStatus.UNMUTED
					}
					item uuid: abonent.uuid.toString(), description: "${abonent.fName} ${abonent.mName} ${abonent.lName} ${abonent.description}",
					status: abonentStatus.toString(), audioToStatus: audioToStatus.toString(), audioFromStatus: audioFromStatus.toString()
				}
			}
		}
	}
	
	//TODO: удалить
	def getAllPalaverUuid() {
		def palavers = Palaver.findAll()
		def palaverUuid = []
		palavers.each { palaverUuid << it.uuid.toString()}
		
		render(contentType: "application/json") {
			palaverUuid
		}
		
	}
}
