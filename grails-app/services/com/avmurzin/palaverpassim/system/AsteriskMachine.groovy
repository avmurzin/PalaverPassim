package com.avmurzin.palaverpassim.system

import java.util.List

import com.avmurzin.palaverpassim.db.Abonent;
import com.avmurzin.palaverpassim.db.Conference;
import com.avmurzin.palaverpassim.db.Phone;

import java.io.IOException;

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
import org.asteriskjava.manager.action.EventsAction
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.action.StatusAction
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.OriginateResponseEvent;
import org.asteriskjava.manager.response.ManagerResponse;

class AsteriskMachine implements CallMachine, ManagerEventListener {

	//This class is singleton
	public static final AsteriskMachine INSTANCE = new AsteriskMachine();
	public static AsteriskMachine getInstance() {
		return INSTANCE;
	}
	private AsteriskMachine() {}

	//	@Override
	public boolean connectToConference(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	//	@Override
	public boolean disconnect(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	//	@Override
	public boolean muteFromAbonent(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	//	@Override
	public boolean muteToAbonent(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	//	@Override
	public boolean unmuteFromAbonent(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	//	@Override
	public boolean unmuteToAbonent(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	//	@Override
	public boolean startRecord(Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	//	@Override
	public boolean stopRecord(Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	//	@Override
	public boolean isAbonentConnected(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return true;
	}

	//	@Override
	public boolean isConferenceBusy(Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	//	@Override
	public boolean isMutedToAbonent(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return true;
	}

	//	@Override
	public boolean isMutedFromAbonent(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return true;
	}

	//	@Override
	public List<Phone> getAbonentConnectedPhone(Abonent abonent,
			Conference conference) {
		// TODO Auto-generated method stub
		return null;
	}

	public void test() {
		ManagerConnection managerConnection;
		ManagerConnectionFactory factory = new ManagerConnectionFactory(
				"localhost", "murzin", "asterisk2015");

		managerConnection = factory.createManagerConnection();

		OriginateAction originateAction;
		StatusAction statusAction;
		EventsAction eventsAction;
		ManagerResponse originateResponse;

		originateAction = new OriginateAction();
		originateAction.setChannel("SIP/as5350gw/989086467383");
		originateAction.setContext("conference");
		originateAction.setExten("2911");
		originateAction.setCallerId("2911");
		originateAction.setPriority(new Integer(1));
		originateAction.setTimeout(new Long(30000));
		originateAction.setActionId("989086467383");
		originateAction.setAsync(true);

		eventsAction = new EventsAction();
		eventsAction.setEventMask("on");

		managerConnection.addEventListener(this);

		// connect to Asterisk and log in
		if(managerConnection.getState() != ManagerConnectionState.CONNECTED) {
			managerConnection.login("on");
		}


		originateResponse = managerConnection.sendAction(eventsAction, 30000);
		println("My message_0: " + "${originateResponse.getResponse()} (${originateResponse.getMessage()}) (${originateResponse.getActionId()})");


		// send the originate action and wait for a maximum of 30 seconds for Asterisk
		// to send a reply
		originateResponse = managerConnection.sendAction(originateAction, 30000);

		originateAction.setChannel("SIP/as5350gw/9513559");
		originateAction.setContext("conference");
		originateAction.setExten("2911");
		originateAction.setCallerId("2911");
		originateAction.setPriority(new Integer(1));
		originateAction.setTimeout(new Long(30000));
		originateAction.setActionId("9513559");
		originateAction.setAsync(true);
		
		originateResponse = managerConnection.sendAction(originateAction, 30000);
		
		originateAction.setChannel("SIP/as5350gw/989086467381");
		originateAction.setContext("conference");
		originateAction.setExten("2911");
		originateAction.setCallerId("2911");
		originateAction.setPriority(new Integer(1));
		originateAction.setTimeout(new Long(30000));
		originateAction.setActionId("989086467381");
		originateAction.setAsync(true);
		
		originateResponse = managerConnection.sendAction(originateAction, 30000);
		
		//	   originateAction = new OriginateAction();
		//	   originateAction.setChannel("SIP/as5350gw/989086467383");
		//	   originateAction.setContext("conference");
		//	   originateAction.setExten("2911");
		//	   originateAction.setPriority(new Integer(1));
		//	   originateAction.setTimeout(new Long(30000));
		//
		//	   originateResponse = managerConnection.sendAction(originateAction, 30000);

		// print out whether the originate succeeded or not
		println("My message: " + "${originateResponse.getResponse()} (${originateResponse.getMessage()}) (${originateResponse.getActionId()})");

		statusAction = new StatusAction();

		originateResponse = managerConnection.sendAction(statusAction, 30000);
		println("My message_2: " + "${originateResponse.getResponse()} (${originateResponse.getMessage()}) (${originateResponse.getEventList()})");
		// and finally log off and disconnect

		//Thread.sleep(10000);

		//managerConnection.logoff();


	}

	@Override
	public void onManagerEvent(ManagerEvent event) {
		
		String channel;
		String conference;

		println event;
		String event_name = event.getClass().getSimpleName();
		//		//println event_name;
		//		println event.toString();
		if (event_name.equals("OriginateResponseEvent")) {
			OriginateResponseEvent orEvent = (OriginateResponseEvent) event;
			channel = orEvent.getChannel();
		}
		
		if (event_name.equals("ConfbridgeJoinEvent")) {
			OriginateResponseEvent orEvent = (OriginateResponseEvent) event;
			channel = orEvent.getChannel();
		}
		////		OriginateResponseEvent orEvent = new OriginateResponseEvent();
		////		orEvent = (OriginateResponseEvent) event;
		////		println ("EVENT:" + orEvent.getActionId() + ":" + orEvent.getChannel())

	}

}
