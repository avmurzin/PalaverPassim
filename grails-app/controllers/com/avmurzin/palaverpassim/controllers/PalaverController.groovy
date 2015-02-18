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

import java.text.SimpleDateFormat
import java.util.UUID;

class PalaverController {

	private final UiManipulation uiManipulation = UiManipulation.getInstance();
	private final CallMachine callMachine = AsteriskMachine.getInstance();

	def index() {
		redirect(url: "/index.html")
	}
	
	def nks() {
		redirect(url: "/nks.html")
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
		long startTimestamp
		long stopTimestamp
		String description = params.description
		
		try {
			startTimestamp = (long) Long.parseLong(params.startTimestamp)
			stopTimestamp = (long) Long.parseLong(params.stopTimestamp)
		} catch (Exception e) {
		render(contentType: "application/json") {
			result = false
			message = "Неверные параметры"
		}
		}

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
					item uuid: abonent.uuid.toString(), description: "${abonent.fName} ${abonent.mName} ${abonent.lName}, ${abonent.description}",
					status: abonentStatus.toString(), iconAbonent: abonentStatus.getIconName(), styleAbonent: abonentStatus.getStyleName(),
					audioToStatus: audioToStatus.toString(), iconTo: audioToStatus.getIconNameTo(), styleTo: audioToStatus.getStyleNameTo(),
					audioFromStatus: audioFromStatus.toString(), iconFrom: audioFromStatus.getIconNameFrom(), styleFrom: audioFromStatus.getStyleNameFrom() 
				}
			}
		}
	}

	/**
	 * Получить список всех палаверов (в виде дерева) в заданный период времени.
	 * ?startTimestamp=&stopTimestamp=
	 * По умолчанию от момента запуска и двое суток вперед.
	 * -Доступ: ${palaverUuid}:WEBUSER
	 * @return
	 */
	def getPalaverTimeline() {
		long startTimestamp
		long stopTimestamp
		Calendar calendar = new GregorianCalendar()
		SimpleDateFormat formattedDate = new SimpleDateFormat("dd.MM HH:MM")

		def tree = []
		def data = []

		try {
			startTimestamp = (long) Long.parseLong(params.startTimestamp)
			stopTimestamp = (long) Long.parseLong(params.stopTimestamp)
		} catch (Exception e) {
			calendar.set(Calendar.HOUR_OF_DAY, 0)
			calendar.set(Calendar.MINUTE, 0)
			calendar.set(Calendar.SECOND, 0)
			startTimestamp = calendar.getTimeInMillis() / 1000
			stopTimestamp = startTimestamp + 2*24*60*60
		}
		
		def conferences = Conference.findAll()

		for (Conference conference : conferences) {
			
			for (Palaver palaver : conference.palaver.findAll { (it.startTimestamp >= startTimestamp) && (it.stopTimestamp <= stopTimestamp)}) {
				calendar.setTimeInMillis(palaver.startTimestamp * 1000)
				String timeFrom = formattedDate.format(calendar.getTime())
				calendar.setTimeInMillis(palaver.stopTimestamp * 1000)
				String timeTo = formattedDate.format(calendar.getTime())
				
				data << [value : "${palaver.description} (${timeFrom} - ${timeTo})"]
			}
			tree << [value: conference.description, data: data]
			data = []
		}

		render(contentType: "application/json") {
			tree
		}
	}

	/**
	 * Получить список всех конференц-комнат.
	 * @return
	 */
	def getConference() {
		def out = []
		def conferences = Conference.findAll().each { out << [id: it.uuid.toString(), value: it.description] }
		render(contentType: "application/json") {
			out
		}
	}
	
	/**
	 * Управление звуком абонента.
	 * $todo = [muteto, unmuteto, mutefrom, unmutefrom]
	 * @return
	 */
	def setAbonentAudio() {
		String todo = params.todo
		UUID uuid = UUID.fromString(params.uuid);
		UUID palaveruuid = UUID.fromString(params.palaveruuid);

		ReturnMessage returnMessage = uiManipulation.setAbonentAudio(uuid, palaveruuid, todo);

		render(contentType: "application/json") {
			result = returnMessage.result
			message = returnMessage.message
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
	
	//TODO: удалить.
	def asterisk() {
		AsteriskMachine asterisk = AsteriskMachine.getInstance();
		//asterisk.test();
		
			render(contentType: "application/json") {
			result = true
			message = "OK"
		}
		
	}
}
