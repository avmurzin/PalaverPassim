package com.avmurzin.palaverpassim.system

import java.util.List

import com.avmurzin.palaverpassim.db.Abonent;
import com.avmurzin.palaverpassim.db.Conference;
import com.avmurzin.palaverpassim.db.Phone;

class AsteriskMachine implements CallMachine {

	@Override
	public boolean connectToConference(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean disconnect(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean muteFromAbonent(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean muteToAbonent(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean startRecord(Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stopRecord(Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAbonentConnected(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConferenceBusy(Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMutedToAbonent(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMutedFromAbonent(Abonent abonent, Conference conference) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Phone> getAbonentConnectedPhone(Abonent abonent,
			Conference conference) {
		// TODO Auto-generated method stub
		return null;
	}

}
