package com.avmurzin.palaverpassim.ui

import com.avmurzin.palaverpassim.db.Abonent
import com.avmurzin.palaverpassim.db.Conference
import com.avmurzin.palaverpassim.db.Palaver
import com.avmurzin.palaverpassim.global.ReturnMessage
import com.avmurzin.palaverpassim.global.Mode
import com.avmurzin.palaverpassim.global.AbonentStatus;
import com.avmurzin.palaverpassim.global.AudioStatus;
import com.avmurzin.palaverpassim.system.CallMachine
import com.avmurzin.palaverpassim.system.AsteriskMachine

class UiManipulation {
	public static final UiManipulation INSTANCE = new UiManipulation();

	public static UiManipulation getInstance() {
		return INSTANCE;
	}

	private UiManipulation() {}

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

		//TODO: проверить занятость расписания на ближайшее будущее и ограничить
		// время проведения palaver. Возможно правильно создать копию палавера
		//с новыми временами начала и окончания (да, так и надо!).

		if(mode == Mode.MANUAL) {
			//в ручном режиме сделать копию палавера, запустить копию
			palaver = Palaver.findByUuid(copyPalaver(uuid));
			//проверить занятость конференции
			if (callMachine.isConferenceBusy(conference)) {
				returnMessage.result = false;
				returnMessage.message = "Конференция занята";
			} else {
				for (Abonent abonent: palaver.abonent.findAll()) {
					//проверка подключенности абонента
					if (!callMachine.getAbonentStatus(abonent, palaver).equals(AbonentStatus.CONNECTED)) {
						callMachine.connectToConference(abonent, palaver);
					}
				}
				returnMessage.result = true;
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
	public boolean isPalaverActive() {
		//TODO: проверить попадает ли текущее время в период проведения встречи
		//
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
		Palaver copyPalaver = new Palaver(uuid: UUID.randomUUID(), 
			description: "${originalPalaver.description} (клон)",
			startTimestamp: originalPalaver.startTimestamp, stopTimestamp: originalPalaver.stopTimestamp,
			abonent: abonent, conference: conference);
		copyPalaver.save(failOnError: true, flush: true);
		return copyPalaver.uuid;
	}
}
