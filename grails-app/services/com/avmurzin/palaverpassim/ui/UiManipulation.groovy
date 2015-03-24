package com.avmurzin.palaverpassim.ui

import java.util.UUID;
import com.avmurzin.palaverpassim.db.Abonent
import com.avmurzin.palaverpassim.db.Conference
import com.avmurzin.palaverpassim.db.Palaver
import com.avmurzin.palaverpassim.global.ReturnMessage
import com.avmurzin.palaverpassim.global.Mode
import com.avmurzin.palaverpassim.global.AbonentStatus;
import com.avmurzin.palaverpassim.global.AudioStatus;
import com.avmurzin.palaverpassim.global.PalaverType;
import com.avmurzin.palaverpassim.system.CallMachine;
import com.avmurzin.palaverpassim.system.AsteriskMachine;
import com.avmurzin.palaverpassim.system.ExecuteCommand;
import java.util.Calendar;

class UiManipulation {
	public static final UiManipulation INSTANCE = new UiManipulation();

	public static UiManipulation getInstance() {
		return INSTANCE;
	}

	private UiManipulation() {}
	
	def config = new ConfigSlurper().parse(new File('ConfigSlurper/palaverpassim.conf').toURI().toURL())

	private final CallMachine callMachine = AsteriskMachine.getInstance();

	/**
	 * Запуск palaver (т.е. вызов всех абонентов) с указанным uuid.
	 * В т.ч. повторный запуск (при добавлении абонента на ходу) с проверкой
	 * уже подключенных абонентов.
	 * @param uuid
	 * @param mode - режим, в котором было вызвано (руками или по расписанию)
	 * @return
	 */
	public ReturnMessage startPalaver(UUID uuid, Mode mode) {

		Palaver palaver = Palaver.findByUuid(uuid);
		Conference conference = palaver.conference;
		ReturnMessage returnMessage = new ReturnMessage();
		returnMessage.result = true;
		//TODO: проверить занятость расписания на ближайшее будущее и ограничить
		// время проведения palaver. Возможно правильно создать копию палавера
		//с новыми временами начала и окончания (да, так и надо!).

		if(mode == Mode.MANUAL) {
			//в ручном режиме сделать копию палавера, запустить копию
			//palaver = Palaver.findByUuid(copyPalaver(uuid));
			//на самом деле копия уже сделана

			//проверить занятость конференции
			if (callMachine.isConferenceBusy(conference)) {
				returnMessage.result = false;
				returnMessage.message = "Конференция занята";
			} else {
				for (Abonent abonent: palaver.abonent.findAll()) {
					//проверка подключенности абонента
					if (!callMachine.getAbonentStatus(abonent, palaver).equals(AbonentStatus.CONNECTED)) {
						returnMessage.result = callMachine.connectToConference(abonent, palaver);
						if(!returnMessage.result) {
							returnMessage.message = "Сервер телефонии не отвечает, обратитесь к администратору";
							return returnMessage;
						}
					}
				}
				returnMessage.message = palaver.uuid.toString(); //"Ожидайте ответа абонентов"
			}
		}
		return returnMessage;
	}

	/**
	 * Останов palaver (отключение всех абонентов).
	 * @param uuid
	 * @return
	 */
	public ReturnMessage stopPalaver(UUID uuid, Mode mode) {
		Palaver palaver = Palaver.findByUuid(uuid);
		Conference conference = palaver.conference;
		ReturnMessage returnMessage = new ReturnMessage();

		for (Abonent abonent: palaver.abonent.findAll()) {
			callMachine.disconnect(abonent, palaver);
		}

		returnMessage.result = true;
		returnMessage.message = "Конференция прекращена";
		return returnMessage;
	}

	/**
	 * Подключиь абенента к палаверу. Если абонента в палавере нет, то добавить.
	 * @param abonent
	 * @param palaver
	 * @return
	 */
	public ReturnMessage connectAbonentToPalaver(Abonent abonent, Palaver palaver) {

		ReturnMessage returnMessage = new ReturnMessage()

		if(!palaver.abonent.contains(abonent)) {
			palaver.abonent << abonent
			palaver.save(failOnError: true, flush: true);
		}

		returnMessage.result = callMachine.connectToConference(abonent, palaver);
		if(!returnMessage.result) {
			returnMessage.message = "Сервер телефонии не отвечает, обратитесь к администратору"
		} else {
			returnMessage.message = "Абонет подключен к конференции";
		}

		return returnMessage;
	}

	/**
	 * Добавить абенента к палаверу, если абонента в палавере нет.
	 * @param abonent
	 * @param palaver
	 * @return
	 */
	public ReturnMessage addAbonentToPalaver(Abonent abonent, Palaver palaver) {

		ReturnMessage returnMessage = new ReturnMessage()

		if(!palaver.abonent.contains(abonent)) {
			palaver.abonent << abonent
			palaver.save(failOnError: true, flush: true);
		}

		returnMessage.result = true
		returnMessage.message = "Абонет добавлен к конференции";


		return returnMessage;
	}
	
	/**
	 * Удалить абонента из палавера
	 * @param abonent
	 * @param palaver
	 * @return
	 */
	public ReturnMessage delAbonentFromPalaver(Abonent abonent, Palaver palaver) {

		ReturnMessage returnMessage = new ReturnMessage()

		if(palaver.abonent.contains(abonent)) {
			palaver.abonent.remove(abonent);
			palaver.save(failOnError: true, flush: true);
		}

		returnMessage.result = true
		returnMessage.message = "Абонет удален из конференции";


		return returnMessage;
	}

	/**
	 * Управление звуком абонента.
	 * @param uuid
	 * @param palaveruuid
	 * @param todo  = [muteto, unmuteto, mutefrom, unmutefrom]
	 * @return
	 */
	public ReturnMessage setAbonentAudio(UUID uuid, UUID palaveruuid, String todo) {
		Palaver palaver = Palaver.findByUuid(palaveruuid);
		Abonent abonent = Abonent.findByUuid(uuid);
		Conference conference = palaver.conference;
		ReturnMessage returnMessage = new ReturnMessage();
		if(todo.equals("muteto")) {
			returnMessage.result = callMachine.setAudioToAbonent(abonent, palaver, AudioStatus.MUTED)
		}
		if(todo.equals("mutefrom")) {
			returnMessage.result = callMachine.setAudioFromAbonent(abonent, palaver, AudioStatus.MUTED)
		}
		if(todo.equals("unmuteto")) {
			returnMessage.result = callMachine.setAudioToAbonent(abonent, palaver, AudioStatus.UNMUTED)
		}
		if(todo.equals("unmutefrom")) {
			returnMessage.result = callMachine.setAudioFromAbonent(abonent, palaver, AudioStatus.UNMUTED)
		}

		if (returnMessage.result) {
			returnMessage.message = "Выполнение операции не удалось";
		} else {
			returnMessage.message = "Операция выполнена успешно";
		}

		return returnMessage;
	}

	/**
	 * Проверка состояния встречи (находится ли в активном состоянии).
	 * @return
	 */
	public boolean isPalaverActive(Palaver palaver) {
		return callMachine.getActivePalaver().contains(palaver)
		//TODO: проверить попадает ли текущее время в период проведения встречи
		//
	}


	/**
	 * Проверка активации сигнальной конференции и запуск палавера.
	 * @return
	 */
	public ReturnMessage checkEvent() {
//		def config = new ConfigSlurper().parse(new File('ConfigSlurper/palaverpassim.conf').toURI().toURL());
//		String conference = "${config.palaverpassim.eventconference}"
//
		ReturnMessage returnMessage = new ReturnMessage(result: true, message: "");
//
//		Palaver eventPalaver;
		Palaver palaver;
		UUID newUuid;
//		//обработка событий в сигнальной конференции
//		//		if(callMachine.getIsEvent()) {
//		if(true) {
//			eventPalaver = Palaver.findByUuid(callMachine.getEventUuid());
//			if((eventPalaver == null) || (!callMachine.getActivePalaver().contains(eventPalaver))) {
//				palaver = Palaver.findByPalaverType(PalaverType.EVENT.toString());
//				newUuid = copyPalaver(palaver.uuid);
//				callMachine.setEventUuid(newUuid);
				//returnMessage = startPalaver(newUuid, Mode.MANUAL);
//				println "${config.palaverpassim.eventpalaver} ${newUuid.toString()}"
//				ExecuteCommand.execute("${config.palaverpassim.eventpalaver} ${newUuid.toString()}");
//			}
//			//			callMachine.setIsEvent(false);
//		}
//		//callMachine.getConfbridgeAbonentList(conference);
//
//		eventPalaver = null;
		palaver = null;
		newUuid = null;
//		config = null;
//		conference = null;

		return returnMessage;

	}

	/**
	 * Проверка расписания, запуск/останов палавера, если наступило время.
	 * @return
	 */
	public ReturnMessage checkTimeline() {
		ReturnMessage returnMessage = new ReturnMessage(result: true, message: "");
		def timelinePalavers;
		Calendar calendar;
		Long nowTime;
		calendar = new GregorianCalendar();
		nowTime = calendar.getTimeInMillis() / 1000;

		callMachine.checkCallMachineConnect();

		//запуск палаверов по расписанию
		timelinePalavers = Palaver.findAll("from Palaver as p where p.startTimestamp <= ? and stopTimestamp >= ?", [nowTime, nowTime]);

		for(Palaver palaver : timelinePalavers) {
			if(!callMachine.getActivePalaver().contains(palaver)) {
				returnMessage = startPalaver(palaver.uuid, Mode.MANUAL);
			}
		}

		for(Palaver palaver : callMachine.getActivePalaver()) {
			if((palaver.stopTimestamp <= nowTime) && (palaver.stopTimestamp != 0)) {
				stopPalaver(palaver.uuid, Mode.MANUAL);
			}
		}

		timelinePalavers = null;
		calendar = null;
		nowTime = null;
		return returnMessage;
	}

	/**
	 * Клонирование палавера.
	 * @param uuid старого палавера.
	 * @return uuid нового палавера.
	 */
	public UUID copyPalaver(UUID uuid) {
		Palaver originalPalaver = Palaver.findByUuid(uuid);
		def abonent = originalPalaver.abonent.findAll();
		def conference = originalPalaver.conference;
		Calendar calendar = new GregorianCalendar()
		long nowTime = calendar.getTimeInMillis() / 1000
		long hour = 3600

		//		Palaver copyPalaver = new Palaver(uuid: UUID.randomUUID(),
		//			description: "${originalPalaver.description} (клон)",
		//			startTimestamp: nowTime, stopTimestamp: nowTime + hour*2,
		//			palaverType: PalaverType.NORMAL,
		//			abonent: abonent, conference: conference);
		Palaver copyPalaver = new Palaver(uuid: UUID.randomUUID(),
		description: "${originalPalaver.description} (клон)",
		startTimestamp: 0, stopTimestamp: 0,
		palaverType: PalaverType.NORMAL,
		abonent: abonent, conference: conference);
		copyPalaver.save(failOnError: true, flush: true);
		return copyPalaver.uuid;
	}
}
