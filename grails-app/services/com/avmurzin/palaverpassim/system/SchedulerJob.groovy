package com.avmurzin.palaverpassim.system

import com.avmurzin.palaverpassim.ui.UiManipulation;
import com.avmurzin.palaverpassim.global.Mode;
import com.avmurzin.palaverpassim.db.Palaver;
import com.avmurzin.palaverpassim.global.PalaverType;

class SchedulerJob {
	def config = new ConfigSlurper().parse(new File('ConfigSlurper/palaverpassim.conf').toURI().toURL());
	String conference = "${config.palaverpassim.eventconference}"
	
	private final AsteriskMachine callMachine = (AsteriskMachine) AsteriskMachine.getInstance();
	private final UiManipulation uiManipulation = UiManipulation.getInstance();

	static triggers = {
		//cron name: 'myTrigger', cronExpression: "0/5 * * * * ?"
		simple name: 'mySimpleTrigger', startDelay: 1000, repeatInterval: 5000
	}
	def group = "PalaverPassim"
	def description = "PalaverPasim repeat"
	def execute(){
		if(callMachine.getIsEvent()) {
			Palaver eventPalaver = Palaver.findByUuid(callMachine.getEventUuid());
			if((eventPalaver == null) || (!callMachine.getActivePalaver().contains(eventPalaver))) {
				Palaver palaver = Palaver.findByPalaverType(PalaverType.EVENT.toString());
				UUID newUuid = uiManipulation.copyPalaver(palaver.uuid);
				callMachine.setEventUuid(newUuid);
				uiManipulation.startPalaver(newUuid, Mode.MANUAL);
				
			}
			callMachine.setIsEvent(false);
		}
		callMachine.getConfbridgeAbonentList(conference);
	}

}
