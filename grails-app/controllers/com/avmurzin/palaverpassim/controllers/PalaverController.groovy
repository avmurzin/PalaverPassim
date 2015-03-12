package com.avmurzin.palaverpassim.controllers

import com.avmurzin.palaverpassim.db.Abonent
import com.avmurzin.palaverpassim.db.Palaver
import com.avmurzin.palaverpassim.db.Conference
import com.avmurzin.palaverpassim.global.AbonentStatus
import com.avmurzin.palaverpassim.global.AudioStatus;
import com.avmurzin.palaverpassim.global.PalaverStatus
import com.avmurzin.palaverpassim.global.ReturnMessage
import com.avmurzin.palaverpassim.global.Mode
import com.avmurzin.palaverpassim.global.PalaverType
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
//		long startTimestamp
//		long stopTimestamp
//		String description = params.description
		UUID uuid = UUID.randomUUID()
		try {
//			startTimestamp = (long) Long.parseLong(params.startTimestamp)
//			stopTimestamp = (long) Long.parseLong(params.stopTimestamp)
			def jsonObject = request.JSON
			UUID confUuid = UUID.fromString(params.uuid)
			def conference = Conference.findByUuid(confUuid)
			def palaver = new Palaver(jsonObject)
			
			//println (palaver.startTimestamp + ":" + palaver.stopTimestamp)
			
			palaver.uuid = uuid
			palaver.conference = conference
			palaver.save(failOnError: true, flush: true)
		} catch (Exception e) {
			render(contentType: "application/json") {
				result = false
				message = "Неверные параметры"
			}
		}
		//def palaver = new Palaver(conference: conference, uuid: uuid, description: description, startTimestamp: startTimestamp, stopTimestamp: stopTimestamp)
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

		ReturnMessage returnMessage = uiManipulation.stopPalaver(uuid, Mode.MANUAL);

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
		UUID puuid
		try {
			puuid = UUID.fromString(params.uuid);
		} catch (Exception e) {
			puuid = UUID.randomUUID()
		}
		

		AbonentStatus abonentStatus = AbonentStatus.DISCONNECTED;
		AudioStatus audioToStatus = AudioStatus.UNMUTED;
		AudioStatus audioFromStatus = AudioStatus.UNMUTED;
		PalaverStatus palaverStatus = PalaverStatus.INACTIVE;

		Palaver palaver = Palaver.findByUuid(puuid);
		
		if(palaver == null) {
			def blah =[]
			render(contentType: "application/json") {
				description = ""
			}
			return
		}
		
		Conference pconference = palaver.conference;
		def abonents = palaver.abonent.findAll();


		render(contentType: "application/json") {
			uuid = puuid.toString()
			description = palaver.description
			palaverType = palaver.palaverType
			conference = pconference.description
			items = array {
				for(Abonent abonent : abonents) {
					abonentStatus = callMachine.getAbonentStatus(abonent, palaver);
					audioToStatus = callMachine.getAudioToAbonent(abonent, palaver);
					audioFromStatus = callMachine.getAudioFromAbonent(abonent, palaver)
					//					if(callMachine.isAbonentConnected(abonent, pconference)) {
					//						abonentStatus = AbonentStatus.CONNECTED
					//					} else {
					//						abonentStatus = AbonentStatus.DISCONNECTED
					//					}
					//					if(callMachine.isMutedFromAbonent(abonent, pconference)) {
					//						audioFromStatus = AudioStatus.MUTED
					//					} else {
					//						audioFromStatus = AudioStatus.UNMUTED
					//					}
					//					if(callMachine.isMutedToAbonent(abonent, pconference)) {
					//						audioToStatus = AudioStatus.MUTED
					//					} else {
					//						audioToStatus = AudioStatus.UNMUTED
					//					}
					item uuid: abonent.uuid.toString(), description: "${abonent.fName} ${abonent.mName} ${abonent.lName}, ${abonent.description} (${abonent.phones.find().phoneNumber})",
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
		SimpleDateFormat formattedDate = new SimpleDateFormat("dd.MM HH:mm")

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

				//println "${palaver.description}: ${palaver.startTimestamp} : ${palaver.stopTimestamp}"
				
				data << [id: "${palaver.uuid.toString()}", image: "group", value : "${palaver.description} (${timeFrom} - ${timeTo})", startTimestamp: "${palaver.startTimestamp}"]
			}
			tree << [image: "door_in", value: conference.description, data: data, startTimestamp: "0"]
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
	 * Отключить абонента от палавера.
	 * /palaver/$palaveruuid/abonent/$uuid/kick
	 * @return
	 */
	def kickAbonentFromPalaver() {
		UUID uuid = UUID.fromString(params.uuid);
		UUID palaveruuid = UUID.fromString(params.palaveruuid);
		def palaver = Palaver.findByUuid(palaveruuid);
		def abonent = Abonent.findByUuid(uuid);

		callMachine.disconnect(abonent, palaver);

		render(contentType: "application/json") {
			result = true
		}
	}

	/**
	 * Подключить абонента к палаверу.
	 * /palaver/$palaveruuid/abonent/$uuid/connect
	 * @return
	 */
	def connectAbonentToPalaver() {
		UUID uuid = UUID.fromString(params.uuid);
		UUID palaveruuid = UUID.fromString(params.palaveruuid);
		def palaver = Palaver.findByUuid(palaveruuid);
		def abonent = Abonent.findByUuid(uuid);

		callMachine.connectToConference(abonent, palaver);

		render(contentType: "application/json") {
			result = true
		}
	}

	/**
	 * Управление звуком абонента.
	 * /abonent/$uuid/$todo/$palaveruuid
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

	/**
	 * Получить дерево шаблонов палаверов. Возвращает JSON-дерево 
	 * для формирования интерфейса выбора шаблона палавер.
	 * @return
	 */
	def getPalaverTemplates() {
		def section = ["${PalaverType.PREPARED.toString()}" : "${PalaverType.PREPARED.getDescription()}", 
			"${PalaverType.TEMPLATE.toString()}" : "${PalaverType.TEMPLATE.getDescription()}"]
		//println section.keySet()
		def tree = []
		def data = []

		for(PalaverType type : PalaverType.values()) {
		
			if(section.get("${type.toString()}") != null) {
				
				for(Palaver palaver : Palaver.findAllByPalaverType("${type.toString()}")) {
					data << [id: "${palaver.uuid.toString()}", image: "group", value: "${palaver.description}", palaverType: "${palaver.palaverType}"]
					
				}
				tree << [image: "application_cascade", value: section.get("${type.toString()}"), open: true, data: data]
				data = []
			}
		}

		render(contentType: "application/json") {
			tree
		}
	}
	
	/**
	 * Выполнить редирект на указанную страницу (с параметрами).
	 * @return
	 */
	def getPage() {
		String page = params.page;
		String uuid = params.uuid;
		
		UUID outUuid = UUID.fromString(uuid);
		
		if(page.equals("template")) {
			Palaver palaver = Palaver.findByUuid(UUID.fromString(uuid))
			if (palaver != null) {
				//если палавер является стартуемым немедленно, то сдалать копию и перейти к странице запуска
				if(palaver.palaverType.equals(PalaverType.PREPARED)) {
					outUuid = uiManipulation.copyPalaver(palaver.uuid)
					page = "palaverControl"
				}
			}
		}
		
		render(view: "/${page}", model: [uuid: outUuid])
	}
	
	/**
	 * Поиск абонента по нескольким введенным символам. Искать по любому текстовому
	 * полю.
	 * @return
	 */
	def findAbonentByText() {
		String text = params.text

		def out = []

		def abonents = Abonent.findAll("from Abonent as a where a.fName like ? or a.mName like ? or a.lName like ? or description like ?",
				["%${text}%", "%${text}%", "%${text}%", "%${text}%"])

		if(abonents.size() != 0){
			for(Abonent abonent : abonents) {
				out << [uuid: "${abonent.uuid.toString()}", description: "${abonent.fName} ${abonent.mName} ${abonent.lName} (${abonent.description})",
					phone: "${abonent.phones.find().phoneNumber}"]
			}

			render(contentType: "application/json") {
				out
			}
		} else {
			render(contentType: "application/json") {
				result = false
			}
		}
	}
	
	/**
	 * Поиск абонента по нескольким введенным символам. Искать по телефону.
	 * @return
	 */
	def findAbonentByPhone() {
		String text = (params.phone =~ /\D/).replaceAll("")
		
		def out = []
		
		def abonents = Abonent.executeQuery("FROM Abonent a WHERE a IN(SELECT p.abonent FROM Phone p WHERE digitalPhoneNumber LIKE ?)", ["%${text}%"])
		
		if(abonents.size() != 0){
			for(Abonent abonent : abonents) {
				out << [uuid: "${abonent.uuid.toString()}", 
					description: "${abonent.fName} ${abonent.mName} ${abonent.lName} (${abonent.description})", 
					phone: "${abonent.phones.find().phoneNumber}"]
			}
			
			render(contentType: "application/json") {
				out
			}
		} else {
			render(contentType: "application/json") {
				result = false
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

	//TODO: удалить.
	def asterisk() {
		AsteriskMachine asterisk = AsteriskMachine.getInstance();
		asterisk.test();

		render(contentType: "application/json") {
			result = true
			message = "OK"
		}

	}
}
