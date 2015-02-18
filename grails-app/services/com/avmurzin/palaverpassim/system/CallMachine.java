package com.avmurzin.palaverpassim.system;

import java.util.List;

import com.avmurzin.palaverpassim.db.Abonent;
import com.avmurzin.palaverpassim.db.Conference;
import com.avmurzin.palaverpassim.db.Phone;

public interface CallMachine {
	
	/**
	 * Подключить абонента к конференции.
	 * @param abonent
	 * @param conference
	 * @return
	 */
	boolean connectToConference(Abonent abonent, Conference conference);
	
	/**
	 * Отключить абонента от конференции.
	 * @param abonent
	 * @param conference
	 * @return
	 */
	boolean disconnect(Abonent abonent, Conference conference);
	
	/**
	 * Заглушить поток от абонента (отключить микрофон) в конференции. 
	 * @param abonent
	 * @param conference
	 * @return
	 */
	boolean muteFromAbonent(Abonent abonent, Conference conference);
	
	/**
	 * Заглушить поток к абоненту (отключить абоненту звук) в конференции.
	 * @param abonent
	 * @param conference
	 * @return
	 */
	boolean muteToAbonent(Abonent abonent, Conference conference);
	
	/**
	 * Включить поток от абонента (включить микрофон) в конференции. 
	 * @param abonent
	 * @param conference
	 * @return
	 */
	boolean unmuteFromAbonent(Abonent abonent, Conference conference);
	
	/**
	 * Включить поток к абоненту (включить абоненту звук) в конференции.
	 * @param abonent
	 * @param conference
	 * @return
	 */
	boolean unmuteToAbonent(Abonent abonent, Conference conference);
	
	/**
	 * начать запись конференции
	 * @param conference
	 * @return
	 */
	boolean startRecord(Conference conference);
	
	/**
	 * остановить запись конференции
	 * @param conference
	 * @return
	 */
	boolean stopRecord(Conference conference);
	
	/**
	 * Проверка подключения абонента к конференции (как минимум одним телефоном).
	 * @param abonent
	 * @param conference
	 * @return
	 */
	boolean isAbonentConnected(Abonent abonent, Conference conference);
	
	/**
	 * Проверить занятость конференции.
	 * @param conference
	 * @return
	 */
	boolean isConferenceBusy(Conference conference);
	
	/**
	 * Проверить наличие звука к абоненту от конференции.
	 * @param abonent
	 * @param conference
	 * @return
	 */
	boolean isMutedToAbonent(Abonent abonent, Conference conference);
	
	/**
	 * Проверить наличие звука от абонента в конференцию.
	 * @param abonent
	 * @param conference
	 * @return
	 */
	boolean isMutedFromAbonent(Abonent abonent, Conference conference);
	
	/**
	 * Получить список телефонов, которыми абонент подключен к конференции.
	 * @param abonent
	 * @param conference
	 * @return
	 */
	List<Phone> getAbonentConnectedPhone(Abonent abonent, Conference conference);
}
