package com.avmurzin.palaverpassim.system;

import java.util.List;

import com.avmurzin.palaverpassim.db.Abonent;
import com.avmurzin.palaverpassim.db.Conference;
import com.avmurzin.palaverpassim.db.Palaver;
import com.avmurzin.palaverpassim.db.Phone;
import com.avmurzin.palaverpassim.global.AbonentStatus;
import com.avmurzin.palaverpassim.global.AudioStatus;

public interface CallMachine {
	
	/**
	 * Подключить абонента к палаверу.
	 * @param abonent
	 * @param palaver
	 * @return
	 */
	boolean connectToConference(Abonent abonent, Palaver palaver);
	
	/**
	 * Отключить абонента от палавера.
	 * @param abonent
	 * @param palaver
	 * @return
	 */
	boolean disconnect(Abonent abonent, Palaver palaver);
	
	/**
	 * Установка состояния аудиоканала к абоненту в палавере.
	 * @param audioStatus
	 * @return
	 */
	boolean setAudioToAbonent(Abonent abonent, Palaver palaver, AudioStatus audioStatus);
	
	/**
	 * Установка состояния аудиоканала от абонента в палавере.
	 * @param audioStatus
	 * @return
	 */
	boolean setAudioFromAbonent(Abonent abonent, Palaver palaver, AudioStatus audioStatus);
	
	/**
	 * Получить статус звукового канала к абоненту в палавере.
	 * @param abonent
	 * @param palaver
	 * @return
	 */
	AudioStatus getAudioToAbonent(Abonent abonent, Palaver palaver);
	
	/**
	 * Получить статус звукового канала от абонента в палавере.
	 * @param abonent
	 * @param palaver
	 * @return
	 */
	AudioStatus getAudioFromAbonent(Abonent abonent, Palaver palaver);
	
//	
//	/**
//	 * Заглушить поток от абонента (отключить микрофон) в палавере. 
//	 * @param abonent
//	 * @param palaver
//	 * @return
//	 */
//	boolean muteFromAbonent(Abonent abonent, Palaver palaver);
//	
//	/**
//	 * Заглушить поток к абоненту (отключить абоненту звук) в палавере.
//	 * @param abonent
//	 * @param palaver
//	 * @return
//	 */
//	boolean muteToAbonent(Abonent abonent, Palaver palaver);
//	
//	/**
//	 * Включить поток от абонента (включить микрофон) в палавере. 
//	 * @param abonent
//	 * @param palaver
//	 * @return
//	 */
//	boolean unmuteFromAbonent(Abonent abonent, Palaver palaver);
//	
//	/**
//	 * Включить поток к абоненту (включить абоненту звук) в палавере.
//	 * @param abonent
//	 * @param palaver
//	 * @return
//	 */
//	boolean unmuteToAbonent(Abonent abonent, Palaver palaver);
	
	/**
	 * Начать запись палавера.
	 * @param palaver
	 * @return
	 */
	boolean startRecord(Palaver palaver);
	
	/**
	 * остановить запись палавера.
	 * @param conference
	 * @return
	 */
	boolean stopRecord(Palaver palaver);
	
//	/**
//	 * Проверка подключения абонента к палаверу (как минимум одним телефоном).
//	 * @param abonent
//	 * @param palaver
//	 * @return
//	 */
//	boolean isAbonentConnected(Abonent abonent, Palaver palaver);
	
	/**
	 * Получить статус абонента в палавере.
	 * @param abonent
	 * @param palaver
	 * @return
	 */
	AbonentStatus getAbonentStatus(Abonent abonent, Palaver palaver);
	
	/**
	 * Проверить занятость конференции.
	 * @param conference
	 * @return
	 */
	boolean isConferenceBusy(Conference conference);
	
//	/**
//	 * Проверить наличие звука к абоненту от конференции.
//	 * @param abonent
//	 * @param conference
//	 * @return
//	 */
//	boolean isMutedToAbonent(Abonent abonent, Conference conference);
//	
//	/**
//	 * Проверить наличие звука от абонента в конференцию.
//	 * @param abonent
//	 * @param conference
//	 * @return
//	 */
//	boolean isMutedFromAbonent(Abonent abonent, Conference conference);
	
	/**
	 * Получить список телефонов, которыми абонент подключен к палаверу.
	 * @param abonent
	 * @param palaver
	 * @return
	 */
	List<Phone> getAbonentConnectedPhone(Abonent abonent, Palaver palaver);
}
