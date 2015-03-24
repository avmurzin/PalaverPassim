package com.avmurzin.palaverpassim.controllers

import com.avmurzin.palaverpassim.db.Abonent
import com.avmurzin.palaverpassim.db.Palaver
import com.avmurzin.palaverpassim.db.Conference
import com.avmurzin.palaverpassim.db.Phone
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
	
	static scope = "singleton"

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
		
		String fName = jsonObject.getString("fName")
		if (fName.equals("")) { fName = "<нет_данных>" }
		
		String mName = jsonObject.getString("mName")
		if (mName.equals("")) { mName = "<нет_данных>" }
		
		String lName = jsonObject.getString("lName")
		if (lName.equals("")) { lName = "<нет_данных>" }
		
		String address = jsonObject.getString("address")
		if (address.equals("")) { address = "<нет_данных>" }
		
		String email = jsonObject.getString("email")
		if (email.equals("")) { email = "email@domain.com" }
		
		String description = jsonObject.getString("description")
		if (description.equals("")) { description = "<нет_данных>" }
		
		String phone = jsonObject.getString("phone")
		
		
		if (phone.equals("") || ((phone =~ /\D/).replaceAll("").equals(""))) { 
			render(contentType: "application/json") {
				result = false
				message = "Номер телефона обязателен!"
			}
		}
		
		def abonent = new Abonent( 
			uuid: uuid,
			fName: fName,
            mName: mName,
            lName: lName,
            description: description,
            address: address,
            email: email);
		
		abonent.save(failOnError: true, flush: true)
		
		def telephone = new Phone(uuid: UUID.randomUUID(),
			phoneNumber: "${phone}",
			description: "-")
		
		telephone.abonent = abonent
		telephone.save(failOnError: true, flush: true)

		render(contentType: "application/json") {
			result = true
			message = uuid.toString()
		}
	}

//	/**
//	 * Создать сессию в указанных временных рамках.
//	 * -Создатель получает права CONF_OWNER.
//	 * -Доступ: ${palaverUuid}:WEBUSER
//	 * @return
//	 */
//	def createPalaver() {
//		//		long startTimestamp
//		//		long stopTimestamp
//		//		String description = params.description
//		UUID uuid = UUID.randomUUID()
//		try {
//			//			startTimestamp = (long) Long.parseLong(params.startTimestamp)
//			//			stopTimestamp = (long) Long.parseLong(params.stopTimestamp)
//			def jsonObject = request.JSON
//			UUID confUuid = UUID.fromString(params.uuid)
//			def conference = Conference.findByUuid(confUuid)
//			def palaver = new Palaver(jsonObject)
//
//			//println (palaver.startTimestamp + ":" + palaver.stopTimestamp)
//
//			palaver.uuid = uuid
//			palaver.conference = conference
//			palaver.save(failOnError: true, flush: true)
//		} catch (Exception e) {
//			render(contentType: "application/json") {
//				result = false
//				message = "Неверные параметры"
//			}
//		}
//		//def palaver = new Palaver(conference: conference, uuid: uuid, description: description, startTimestamp: startTimestamp, stopTimestamp: stopTimestamp)
//		render(contentType: "application/json") {
//			result = true
//			message = uuid.toString()
//		}
//	}

	/**
	 * Обновить данные о палавере.
	 * -Создатель получает права CONF_OWNER.
	 * -Доступ: ${palaverUuid}:WEBUSER
	 * @return
	 */
	def updatePalaver() {

		try {
			def uuid = params.uuid;
			def jsonObject = request.JSON
			UUID conferenceUuid = UUID.fromString(jsonObject.getString("conferenceUuid"))
			Conference conference = Conference.findByUuid(conferenceUuid)
			Palaver palaver = Palaver.findByUuid(UUID.fromString(uuid))

			palaver.conference = conference
			palaver.startTimestamp = jsonObject.getLong("startTimestamp")
			palaver.stopTimestamp = jsonObject.getLong("stopTimestamp")
			palaver.description = jsonObject.getString("description")
			palaver.palaverType = jsonObject.getString("palaverType")
			
			if(!palaver.palaverType.equals("NORMAL")) {
				palaver.startTimestamp = 0
				palaver.stopTimestamp = 0
			}
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
	 * Удалить палавер.
	 * @return
	 */
	def deletePalaver() {
		Palaver palaver;
		try {

			palaver = Palaver.findByUuid(UUID.fromString(params.uuid))

		} catch (Exception e) {
			render(contentType: "application/json") {
				result = false
				message = "Неверные параметры"
			}
		}
		if(palaver != null) {
			palaver.delete(flush: true)
			render(contentType: "application/json") {
				result = true
				message = ""
			}
		} else {
			render(contentType: "application/json") {
				result = false
				message = "Неверные параметры"
			}
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
	
	def secretStartPalaver() {
//		//UUID uuid = UUID.fromString(params.uuid);
//
//
//
//
//		ReturnMessage returnMessage = uiManipulation.startPalaver(uiManipulation.copyPalaver(Palaver.findByPalaverType(PalaverType.EVENT.toString()).uuid), Mode.MANUAL);
//
//		render(contentType: "application/json") {
//			result = returnMessage.result
//			message = returnMessage.message
//		}
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
			startTimestamp = palaver.startTimestamp;
			stopTimestamp = palaver.stopTimestamp;
			items = array {
				for(Abonent abonent : abonents) {
					abonentStatus = callMachine.getAbonentStatus(abonent, palaver);
					audioToStatus = callMachine.getAudioToAbonent(abonent, palaver);
					audioFromStatus = callMachine.getAudioFromAbonent(abonent, palaver)

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

		ReturnMessage returnMessage = uiManipulation.connectAbonentToPalaver(abonent, palaver);

		render(contentType: "application/json") {
			result = returnMessage.result
			message = returnMessage.message
		}
	}
	
	/**
	 * Добавить абонента к палаверу.
	 * /palaver/$palaveruuid/abonent/$uuid (POST)
	 * @return
	 */
	def addAbonentToPalaver() {
		UUID uuid = UUID.fromString(params.uuid);
		UUID palaveruuid = UUID.fromString(params.palaveruuid);
		def palaver = Palaver.findByUuid(palaveruuid);
		def abonent = Abonent.findByUuid(uuid);

		ReturnMessage returnMessage = uiManipulation.addAbonentToPalaver(abonent, palaver);

		render(contentType: "application/json") {
			result = returnMessage.result
			message = returnMessage.message
		}
	}
	
	/**
	 * Удалить абонента из палавера.
	 * /palaver/$palaveruuid/abonent/$uuid (DELETE)
	 * @return
	 */
	def delAbonentFromPalaver() {
		UUID uuid = UUID.fromString(params.uuid);
		UUID palaveruuid = UUID.fromString(params.palaveruuid);
		def palaver = Palaver.findByUuid(palaveruuid);
		def abonent = Abonent.findByUuid(uuid);

		ReturnMessage returnMessage = uiManipulation.delAbonentFromPalaver(abonent, palaver);

		render(contentType: "application/json") {
			result = returnMessage.result
			message = returnMessage.message
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
		
		boolean edit = false
		
		try {
			if (params.edit.equals("true")) {
				edit = true
			}
		} catch (Exception e) {

		}
		
		def section = ["${PalaverType.PREPARED.toString()}" : "${PalaverType.PREPARED.getDescription()}",
			"${PalaverType.TEMPLATE.toString()}" : "${PalaverType.TEMPLATE.getDescription()}"]
		
		if(edit) {
			section << ["${PalaverType.EVENT.toString()}" : "${PalaverType.EVENT.getDescription()}"]
		}
		
		def tree = []
		def data = []

		for(PalaverType type : PalaverType.values()) {

			if(section.get("${type.toString()}") != null) {

				for(Palaver palaver : Palaver.findAllByPalaverType("${type.toString()}")) {
					data << [id: "${palaver.uuid.toString()}", image: "group", value: "${palaver.description} (${palaver.conference.description})", palaverType: "${palaver.palaverType}"]

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
		//println "test___"
		UUID outUuid = UUID.fromString(uuid);

		if(page.equals("template")) {
			Palaver palaver = Palaver.findByUuid(UUID.fromString(uuid))
			if (palaver != null) {
				//если палавер является стартуемым немедленно, то сдалать копию и перейти к странице запуска
				if(palaver.palaverType.equals(PalaverType.PREPARED.toString())) {
					outUuid = uiManipulation.copyPalaver(palaver.uuid)
					page = "palaverControl"
				}
				//если палавер является шаблоном, то сдалать копию и перейти к странице редактирования
				if(palaver.palaverType.equals(PalaverType.TEMPLATE.toString())) {
					outUuid = uiManipulation.copyPalaver(palaver.uuid)
					page = "edit"
				}
			}
		}
		
//		if(page.equals("edit")) {
//			Palaver palaver = Palaver.findByUuid(UUID.fromString(uuid))
//			if (palaver != null) {
//				//если палавер является шаблоном, то сдалать копию и перейти к странице редактирования
//				if(palaver.palaverType.equals(PalaverType.TEMPLATE.toString())) {
//					outUuid = uiManipulation.copyPalaver(palaver.uuid)
//					page = "edit"
//				}
//				//если палавер является нормальным, то копию не делать и перейти к странице редактирования
//				if(palaver.palaverType.equals(PalaverType.NORMAL.toString())) {
//					outUuid = palaver.uuid.toString()
//					page = "edit"
//				}
//			}
//		}

		render(view: "/${page}", model: [uuid: outUuid])
	}

	/**
	 * Поиск абонента по нескольким введенным символам. Искать по любому текстовому
	 * полю, но только если введенные символы присутствуют.
	 * @return
	 */
	def findAbonentByText() {
		String text = params.text

		if(text.equals("")) {
			render(contentType: "application/json") {
				result = false
			}
		}

		def out = []

		def abonents = Abonent.findAll("from Abonent as a where a.fName like ? or a.mName like ? or a.lName like ? or description like ?",
				["%${text}%", "%${text}%", "%${text}%", "%${text}%"])

		if(abonents.size() != 0){
			for(Abonent abonent : abonents) {
				out << [id: "${abonent.uuid.toString()}", description: "${abonent.fName} ${abonent.mName} ${abonent.lName} (${abonent.description})",
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
	 * Но только если введенные символы присутсвуют.
	 * @return
	 */
	def findAbonentByPhone() {
		String text = (params.phone =~ /\D/).replaceAll("")

		def out = []

		if(text.equals("")) {
			render(contentType: "application/json") {
				result = false
			}
		}

		def abonents = Abonent.executeQuery("FROM Abonent a WHERE a IN(SELECT p.abonent FROM Phone p WHERE digitalPhoneNumber LIKE ?)", ["%${text}%"])

		if(abonents.size() != 0){
			for(Abonent abonent : abonents) {
				out << [id: "${abonent.uuid.toString()}",
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

	/**
	 * Получить список всех активных палаверов на текущий момент.
	 * @return JSON объект со списком палаверов (визуально - дерево)
	 */
	def getActivePalaver() {
		def tree = []
		def data = []
		def data2 = []

		for(Palaver palaver : callMachine.getActivePalaver()) {
			for(Abonent abonent : palaver.abonent) {
				data2 << [id: "${abonent.uuid.toString()}", image: "phone",
					value: "${abonent.fName} ${abonent.mName} ${abonent.lName} (${abonent.description})"]
			}
			data << [id: "${palaver.uuid.toString()}", image: "group", value: "${palaver.description}", 
				palaverType: "${palaver.palaverType}", open: true, data: data2]
			data2 = []
		}

		tree << [image: "application_cascade", value: "Активные в текущий момент конференции", open: true, data: data]
		data = []
		
		render(contentType: "application/json") {
			tree
		}
	}

	def exportAbonent() {
		String fileName = params.fileName;
		try {
			def csv = new File(fileName)
			csv.splitEachLine(';') { row ->
				def	abonent = new Abonent(
						uuid: UUID.randomUUID(),
						lName: "${row[0].take(50)}" ?: "-",
						fName: "${row[1].take(50)}" ?: "-",
						mName: "${row[2].take(50)}" ?: "-",
						description: "${row[3].take(400)}" ?: "-",
						address: "${row[4]}, ${row[5]}" ?: "-",
						email: "no_data"
						)
				abonent.save(failOnError: true, flush: true)
				
				def phone = new Phone(uuid: UUID.randomUUID(),
					phoneNumber: "${row[6]}" ?: "-",
					description: "рабочий")
					phone.abonent = abonent
					phone.save(failOnError: true, flush: true)
				
			}
			render(contentType: "application/json") {
				result = true
				message = "Exported"
			}
		} catch (Exception e) {
			render(contentType: "application/json") {
				result = false
				message = "Error open file"
			}
		}

	}
	
	/**
	 * Проверка расписания и запуск/останов палавера, если наступило время.
	 * @return
	 */
	def checkTimeline() {
		render(contentType: "application/json") {
			result = uiManipulation.checkTimeline().result;
		}
	}
	
	/**
	 * Проверка сигнальной конферецнии и запуск палавера, если появился абонент.
	 * @return
	 */
	def checkEvent() {
		render(contentType: "application/json") {
			result = uiManipulation.checkEvent().result;
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
