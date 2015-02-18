package com.avmurzin.palaverpassim.system

import java.util.List

import com.avmurzin.palaverpassim.db.Abonent;
import com.avmurzin.palaverpassim.db.Conference;
import com.avmurzin.palaverpassim.db.Phone;
import java.io.IOException;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;

class AsteriskMachine implements CallMachine {

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
			
//	public void test() {
//		ManagerConnection managerConnection;
//		ManagerConnectionFactory factory = new ManagerConnectionFactory(
//			"localhost", "murzin", "asterisk2015");
//
//	   managerConnection = factory.createManagerConnection();
//	   
//	   OriginateAction originateAction;
//	   ManagerResponse originateResponse;
//
//	   originateAction = new OriginateAction();
//	   originateAction.setChannel("ooh323/9513559");
//	   originateAction.setContext("outgoing");
//	   originateAction.setExten("2911");
//	   originateAction.setPriority(new Integer(1));
//	   originateAction.setTimeout(new Integer(30000));
//
//	   // connect to Asterisk and log in
//	   managerConnection.login();
//
//	   // send the originate action and wait for a maximum of 30 seconds for Asterisk
//	   // to send a reply
//	   originateResponse = managerConnection.sendAction(originateAction, 30000);
//
//	   // print out whether the originate succeeded or not
//	  println("My mesage: " + originateResponse.getResponse());
//
//	   // and finally log off and disconnect
//  
//	   managerConnection.logoff();
//	}

}
