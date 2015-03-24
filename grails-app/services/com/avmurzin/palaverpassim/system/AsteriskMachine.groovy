package com.avmurzin.palaverpassim.system

import java.util.List

import com.avmurzin.palaverpassim.db.Abonent;
import com.avmurzin.palaverpassim.db.Conference;
import com.avmurzin.palaverpassim.db.Palaver;
import com.avmurzin.palaverpassim.db.Phone;
import com.avmurzin.palaverpassim.global.AbonentStatus;
import com.avmurzin.palaverpassim.global.AudioStatus;
import com.avmurzin.palaverpassim.ui.UiManipulation;
import com.avmurzin.palaverpassim.global.PalaverType;
import java.io.IOException;
import com.avmurzin.palaverpassim.global.Mode
import org.asteriskjava.live.AsteriskChannel
import org.asteriskjava.live.AsteriskQueue
import org.asteriskjava.live.AsteriskServer
import org.asteriskjava.live.DefaultAsteriskServer
import org.asteriskjava.live.MeetMeRoom
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerEventListener
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.CommandAction
import org.asteriskjava.manager.action.ConfbridgeKickAction
import org.asteriskjava.manager.action.ConfbridgeListAction
import org.asteriskjava.manager.action.ConfbridgeListRoomsAction
import org.asteriskjava.manager.action.ConfbridgeMuteAction
import org.asteriskjava.manager.action.ConfbridgeStartRecordAction
import org.asteriskjava.manager.action.ConfbridgeUnmuteAction
import org.asteriskjava.manager.action.EventsAction
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.action.StatusAction
import org.asteriskjava.manager.event.ConfbridgeJoinEvent
import org.asteriskjava.manager.event.ConfbridgeListEvent
import org.asteriskjava.manager.event.ConfbridgeListRoomsEvent
import org.asteriskjava.manager.event.ConfbridgeStartEvent
import org.asteriskjava.manager.event.HangupEvent
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.OriginateResponseEvent;
import org.asteriskjava.manager.response.CommandResponse
import org.asteriskjava.manager.response.ManagerResponse;

class AsteriskMachine implements CallMachine, ManagerEventListener {

	private boolean isEvent = false;
	private UUID eventUuid = UUID.randomUUID();

	public void setIsEvent(boolean isEvent) {
		this.isEvent = isEvent;
	}

	public boolean getIsEvent() {
		return this.isEvent;
	}

	public void setEventUuid(UUID eventUuid) {
		this.eventUuid = eventUuid;
	}

	public UUID getEventUuid() {
		return this.eventUuid;
	}

	def config = new ConfigSlurper().parse(new File('ConfigSlurper/palaverpassim.conf').toURI().toURL())
	ManagerConnectionFactory factory = new ManagerConnectionFactory(
	"${config.asterisk.host}", "${config.asterisk.login}", "${config.asterisk.password}");

	ManagerConnection managerConnection = factory.createManagerConnection();


	class Member {
		UUID uuid;
		String channel = "";
		String actionId = "";
		AbonentStatus abonentStatus = AbonentStatus.DISCONNECTED;
		AudioStatus audioToStatus = AudioStatus.MUTED;
		AudioStatus audioFromStatus = AudioStatus.MUTED;;
		String phone = "";
	}

	class Room {
		UUID uuid;
		String confBridge;
		Map<UUID, Member> member = new HashMap<UUID, Member>();
	}

	private Map<UUID, Room> roomMap;

	//This class is singleton
	public static final AsteriskMachine INSTANCE = new AsteriskMachine();
	public static AsteriskMachine getInstance() {
		return INSTANCE;
	}

	private AsteriskMachine() {
		roomMap = new HashMap<UUID, Member>();
		managerConnection.addEventListener(this);
	}


	//	@Override
	public boolean connectToConference(Abonent abonent, Palaver palaver) {
		Room room;
		Member member;
		String actionId;
		String channel;
		OriginateAction originateAction;
		ManagerResponse originateResponse;

		// connect to Asterisk and log in
		if(managerConnection.getState() != ManagerConnectionState.CONNECTED) {

			try {
				managerConnection.login("on");
			} catch (Exception e) {
				return false;
			}

		}

		//проверить есть ли палавер в запущенных, создать если нет.
		if(roomMap.get(palaver.uuid) == null) {
			room = new Room();
			room.uuid = palaver.uuid;
			roomMap.put(palaver.uuid, room);

			println "Палавера нет, создали новый. В комнате ${room.member.size()} абонентов"

			//изменить время начала на фактическое
			Calendar calendar = new GregorianCalendar();
			long nowTime = calendar.getTimeInMillis() / 1000;
			palaver.startTimestamp = nowTime;
			palaver.save(failOnError: true, flush: true);
		} else {
			room = roomMap.get(palaver.uuid);

			println "Палавер уже есть, используем ${palaver.uuid.toString()}. В комнате ${room.member.size()} абонентов"

		}

		//проверить есть ли абонент в руме, создать если нет
		if(room.member.get(abonent.uuid) == null) {
			member = new Member();
			member.uuid = abonent.uuid;
			//TODO: расширенные правила набора и проверки номера(?)
			member.phone = (abonent.phones.find().phoneNumber =~ /\D/).replaceAll("");

			if(member.phone ==~ /^[^09]\d{6,}/) {
				member.phone = "9${member.phone}"
			}

			member.actionId = member.uuid.toString();
			room.member.put(abonent.uuid, member);

			println "Абонента еще нет, создаем. В комнате ${room.member.size()} абонентов"

		} else {
			member = room.member.get(abonent.uuid);

			println "Абонент уже есть, используем ${member.phone}. В комнате ${room.member.size()} абонентов"

		}

		//если абонент не в коннекте, подключаем
		if(!member.abonentStatus.equals(AbonentStatus.CONNECTED)) {
			originateAction = new OriginateAction();

			//channel = "SIP/as5350gw/${member.phone}";
			channel = "Local/${member.phone}@outgoing/n";

			//			if(member.phone ==~ /^1[3498]\d+/) {
			//				channel = "SIP/as5350gw/${member.phone}";
			//			} else {
			//				channel = "SIP/as5350gw/${member.phone}";
			//			}
			//channel = "${config.asterisk.channel}/${member.phone}@${config.asterisk.context}"
			//			for (i in 0..config.channelRules.rules.size() - 1) {
			//				if("9513559" ==~ config.channelRules.rules[i].regex) {
			//					println config.channelRules.rules[i].channel
			//				}
			//			}

			//originateAction.setChannel("${channel}/${member.phone}");
			originateAction.setChannel("${channel}");
			originateAction.setContext("conference");
			originateAction.setExten("${palaver.conference.phoneNumber}");
			originateAction.setCallerId("${palaver.conference.phoneNumber}");
			originateAction.setPriority(new Integer(1));
			originateAction.setTimeout(new Long(30000));
			originateAction.setActionId("${member.actionId}");
			originateAction.setAsync(true);



			member.abonentStatus = AbonentStatus.INPROCESS;
			originateResponse = managerConnection.sendAction(originateAction, 30000);

		}

		return true;
	}



	//	@Override
	public boolean disconnect(Abonent abonent, Palaver palaver) {
		Room room;
		Member member;
		ConfbridgeKickAction confbridgeKickAction;
		ManagerResponse originateResponse;

		// connect to Asterisk and log in
		if(managerConnection.getState() != ManagerConnectionState.CONNECTED) {
			try {
				managerConnection.login("on");
			} catch (Exception e) {
				return false;
			}
		}
		//проверить есть ли палавер и абонент в запущенных
		if(roomMap.get(palaver.uuid) != null) {
			room = roomMap.get(palaver.uuid);
			if(room.member.get(abonent.uuid) != null) {
				member = room.member.get(abonent.uuid);
				member.abonentStatus = AbonentStatus.INPROCESS;
				confbridgeKickAction = new ConfbridgeKickAction(room.confBridge, member.channel);
				originateResponse = managerConnection.sendAction(confbridgeKickAction, 30000);
			}
			//и если абонент был последний, то всё почистить
			if(room.member.size() == 0) {
				clearPalaver(room.getUuid())
			}
		} else {
			return false;
		}
		return true;
	}


	//	@Override
	public boolean setAudioToAbonent(Abonent abonent, Palaver palaver, AudioStatus audioStatus) {
		// TODO Auto-generated method stub
		return false;
	}



	//	@Override
	public boolean setAudioFromAbonent(Abonent abonent, Palaver palaver, AudioStatus audioStatus) {
		Room room;
		Member member;
		if (roomMap.get(palaver.uuid) != null) {
			room = roomMap.get(palaver.uuid);
			if((room.member.get(abonent.uuid) != null) && (!room.confBridge.equals(""))) {
				member = room.member.get(abonent.uuid);

				if(audioStatus.equals(AudioStatus.MUTED)) {
					ConfbridgeMuteAction confbridgeMuteAction = new ConfbridgeMuteAction(room.confBridge, member.channel);
					managerConnection.sendAction(confbridgeMuteAction, 30000);
					member.audioFromStatus = AudioStatus.MUTED;
				}
				if(audioStatus.equals(AudioStatus.UNMUTED)) {
					ConfbridgeUnmuteAction confbridgeUnmuteAction = new ConfbridgeUnmuteAction(room.confBridge, member.channel);
					managerConnection.sendAction(confbridgeUnmuteAction, 30000);
					member.audioFromStatus = AudioStatus.UNMUTED;
				}

			}
		}
		return false;
	}

	//	@Override
	public AudioStatus getAudioToAbonent(Abonent abonent, Palaver palaver) {
		Room room;
		if (roomMap.get(palaver.uuid) != null) {
			room = roomMap.get(palaver.uuid);
			if(room.member.get(abonent.uuid) != null) {
				return room.member.get(abonent.uuid).audioToStatus;
			}
		}
		return AudioStatus.MUTED;
	}



	//	@Override
	public AudioStatus getAudioFromAbonent(Abonent abonent, Palaver palaver) {
		Room room;
		if (roomMap.get(palaver.uuid) != null) {
			room = roomMap.get(palaver.uuid);
			if(room.member.get(abonent.uuid) != null) {
				return room.member.get(abonent.uuid).audioFromStatus;
			}
		}
		return AudioStatus.MUTED;
	}



	//	@Override
	public boolean startRecord(Palaver palaver) {
		// TODO Auto-generated method stub
		return false;
	}



	//	@Override
	public boolean stopRecord(Palaver palaver) {
		// TODO Auto-generated method stub
		return false;
	}



	//	@Override
	public AbonentStatus getAbonentStatus(Abonent abonent, Palaver palaver) {
		Room room;
		if (roomMap.get(palaver.uuid) != null) {
			room = roomMap.get(palaver.uuid);
			if(room.member.get(abonent.uuid) != null) {
				return room.member.get(abonent.uuid).abonentStatus;
			}
		}
		return AbonentStatus.DISCONNECTED;
	}



	//	@Override
	public boolean isConferenceBusy(Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}



	//	@Override
	public List<Phone> getAbonentConnectedPhone(Abonent abonent, Palaver palaver) {
		// TODO Auto-generated method stub
		return null;
	}

	//TODO: Удалить
	public void test() {
		//getConfbridgeList();
		getConfbridgeAbonentList("2900");
	}

	/**
	 * Очистить конференцию (т.е. отключить абонентов, если есть, очистить все структуры данных, проставить время завершения).
	 * @param palaver
	 * @return
	 */
	public boolean clearPalaver(Palaver palaver) {
		return clearPalaver(palaver.uuid);
	}

	public boolean clearPalaver(UUID uuid) {
		Room room;
		//Member member;
		ConfbridgeKickAction confbridgeKickAction;
		ManagerResponse originateResponse;

		// connect to Asterisk and log in
		if(managerConnection.getState() != ManagerConnectionState.CONNECTED) {
			try {
				managerConnection.login("on");
			} catch (Exception e) {
				return false;
			}
		}

		//проверить есть ли палавер в запущенных. Если есть, кикнуть всех абонентов.
		if(roomMap.get(uuid) != null) {
			room = roomMap.get(uuid);
			for(Member member : room.member.values()) {
				confbridgeKickAction = new ConfbridgeKickAction(room.confBridge, member.channel);
				originateResponse = managerConnection.sendAction(confbridgeKickAction, 30000);
			}
			//удалить запись о палавере и проставить время окончания.
			roomMap.remove(room.uuid);
			Palaver palaver = Palaver.findByUuid(room.uuid);
			//изменить время завершения на фактическое
			Calendar calendar = new GregorianCalendar();
			long nowTime = calendar.getTimeInMillis() / 1000;
			palaver.stopTimestamp = nowTime;
			palaver.save(failOnError: true, flush: true);
			println "Завершаем конференцию"
		} else {
			return true;
		}
		return true;
	}

	/**
	 * Получить список всех активных в текущий момент палаверов.
	 * @return List<Palaver>
	 */
	public List<Palaver> getActivePalaver() {
		List<Palaver> palaverList = new ArrayList<Palaver>()
		for(UUID uuid : roomMap.keySet()) {
			if(Palaver.findByUuid(uuid) != null) {
				palaverList << Palaver.findByUuid(uuid)
			}

		}
		return palaverList;
	}

	/**
	 * Запросить перечень конф-бриджей.
	 */
	public void getConfbridgeList() {
		ConfbridgeListRoomsAction confbridgeListRoomsAction;
		ManagerResponse originateResponse;

		// connect to Asterisk and log in
		if(managerConnection.getState() != ManagerConnectionState.CONNECTED) {
			try {
				managerConnection.login("on");
			} catch (Exception e) {
				return;
			}
		}
		confbridgeListRoomsAction = new ConfbridgeListRoomsAction();
		originateResponse = managerConnection.sendAction(confbridgeListRoomsAction, 30000);
	}

	/**
	 * Запросить перечень абонентов конктретного конф-бриджа.
	 */
	public void getConfbridgeAbonentList(String conferenceID) {
		ConfbridgeListAction confbridgeListAction;
		ManagerResponse originateResponse;
		//
		// connect to Asterisk and log in
		if(managerConnection.getState() != ManagerConnectionState.CONNECTED) {
			try {
				managerConnection.login("on");
			} catch (Exception e) {
				return;
			}
		}

		//println "getConfbridgeAbonentList - " + conferenceID;
		confbridgeListAction = new ConfbridgeListAction(conferenceID);
		originateResponse = managerConnection.sendAction(confbridgeListAction, 30000);
	}

	/**
	 * 
	 * @return
	 */
	public boolean checkCallMachineConnect() {
		// connect to Asterisk and log in
		if(managerConnection.getState() != ManagerConnectionState.CONNECTED) {
			try {
				managerConnection.login("on");
			} catch (Exception e) {
				return false;
			}
		}
	}

	/**
	 * Реакция на события.
	 */
	@Override
	public void onManagerEvent(ManagerEvent event) {

		String channel;
		String conference;
		Member member;

		String event_name = event.getClass().getSimpleName();

		if (!event_name.equals("RtcpSentEvent") && !event_name.equals("RtcpReceivedEvent")) {
			println event;
		}

		//		if (event_name.equals("ConfbridgeListRoomsEvent")) {
		//			//println event;
		//			ConfbridgeListRoomsEvent listRoomEvent = (ConfbridgeListRoomsEvent) event;
		//			String conferenceId = listRoomEvent.getConference();
		//			println "BridgeID = " + conferenceId;
		//			for(int i = 1; listRoomEvent.getParties(); i++) {
		//			}
		//		}

		if (event_name.equals("ConfbridgeListEvent")) {
			ConfbridgeListEvent listEvent = (ConfbridgeListEvent) event;
			//isEvent = true;
		}

		if (event_name.equals("OriginateResponseEvent")) {
			println event;
			OriginateResponseEvent orEvent = (OriginateResponseEvent) event;
			channel = orEvent.getChannel();

			for (Room room : roomMap.values()) {

				if(room.member.get(UUID.fromString(orEvent.getActionId())) != null) {
					member = room.member.get(UUID.fromString(orEvent.getActionId()));
					if(orEvent.isSuccess()) {
						member.abonentStatus = AbonentStatus.CONNECTED;
						member.audioToStatus = AudioStatus.UNMUTED;
						member.audioFromStatus = AudioStatus.UNMUTED;
						member.channel = channel;
					} else {
						member.abonentStatus = AbonentStatus.DISCONNECTED;
						member.audioToStatus = AudioStatus.MUTED;
						member.audioFromStatus = AudioStatus.MUTED;
						//пожалуй правильнее будет удалить абонента из списка живых вообще
						room.member.remove(UUID.fromString(orEvent.getActionId()));
						println "Абонент ${member.phone} отключен, удаляется. В комнате ${room.member.size()} абонентов"
						//и если абонент был последний, то всё почистить
						if(room.member.size() == 0) {
							clearPalaver(room.getUuid())
						}

					}
				}
			}
		}

		if (event_name.equals("HangupEvent")) {
			println event;
			HangupEvent huEvent = (HangupEvent) event;
			channel = huEvent.getChannel();

			for (Room room : roomMap.values()) {
				for (Member abonent : room.member.values()) {
					if(abonent.channel.equals(channel)) {
						room.member.remove(abonent.uuid);

						println "Абонент ${abonent.phone} отключен, удаляется. В комнате ${room.member.size()} абонентов"
						//и если абонент был последний, то всё почистить
						if(room.member.size() == 0) {
							clearPalaver(room.getUuid())
						}
					}
				}
			}
		}

		if (event_name.equals("ConfbridgeJoinEvent")) {
			println event;
			ConfbridgeJoinEvent cbEvent = (ConfbridgeJoinEvent) event;
			channel = cbEvent.getChannel();

			for (Room room : roomMap.values()) {
				for (Member abonent : room.member.values()) {
					if(abonent.channel.equals(channel)) {
						//room.member.remove(abonent.uuid);
						room.confBridge = cbEvent.getConference();
					}
				}
			}
		}

		if(event_name.equals("ConfbridgeStartEvent")) {
			UiManipulation uiManipulation = UiManipulation.getInstance();
			ConfbridgeStartEvent cbsEvent = (ConfbridgeStartEvent) event;

			if(cbsEvent.getConference() == "${config.palaverpassim.eventconference}") {
				println "Event - ${config.palaverpassim.eventconference}"
				//isEvent = true;
//				ExecuteCommand.execute("${config.palaverpassim.eventscript}");
//				ExecuteCommand.execute("${config.palaverpassim.eventpalaver}");
			}

			//ConfbridgeStartRecordAction confbridgeStartRecordAction = new ConfbridgeStartRecordAction(cbsEvent.getConference());
			//managerConnection.sendAction(confbridgeStartRecordAction, 30000);
		}

		for(Room room : roomMap.values()) {
			if(room.member.empty) {
				roomMap.remove(room.uuid);
				Palaver palaver = Palaver.findByUuid(room.uuid);
				//изменить время завершения на фактическое
				Calendar calendar = new GregorianCalendar();
				long nowTime = calendar.getTimeInMillis() / 1000;
				palaver.stopTimestamp = nowTime;
				palaver.save(failOnError: true, flush: true);
				println "Завершаем конференцию"
			}
		}


	}

}
