package com.avmurzin.palaverpassim.system

import java.util.UUID;

import com.avmurzin.palaverpassim.ui.UiManipulation;
import com.avmurzin.palaverpassim.global.Mode;
import com.avmurzin.palaverpassim.db.Palaver;
import com.avmurzin.palaverpassim.global.PalaverType;

class TimelineJob {
//	
//	private final AsteriskMachine callMachine = (AsteriskMachine) AsteriskMachine.getInstance();
//	private final UiManipulation uiManipulation = UiManipulation.getInstance();
//	
//	def timelinePalavers;
//	Calendar calendar;
//	long nowTime;
////	Palaver palaver;
////	UUID newUuid;
//
//	static triggers = {
//		cron name: 'myTrigger', cronExpression: "0 0/1 * * * ?"
//		//simple name: 'mySimpleTrigger', startDelay: 1000, repeatInterval: 5000
//	}
//	def group = "PalaverPassim timeline"
//	def description = "PalaverPasim timeline"
//	def execute(){
//		//запуск палаверов по расписанию
//		
//		calendar = new GregorianCalendar();
//		nowTime = calendar.getTimeInMillis() / 1000;
//		
//		timelinePalavers = Palaver.findAll("from Palaver as p where p.startTimestamp <= ? and stopTimestamp >= ?", [nowTime, nowTime]);
//		
//		for(Palaver palaver : timelinePalavers) {
//			if(!callMachine.getActivePalaver().contains(palaver)) {
//				uiManipulation.startPalaver(palaver.uuid, Mode.MANUAL);
//			}
//		}
//		
//		for(Palaver palaver : callMachine.getActivePalaver()) {
//			if((palaver.stopTimestamp <= nowTime) && (palaver.stopTimestamp != 0)) {
//				uiManipulation.stopPalaver(palaver.uuid, Mode.MANUAL);
//			}
//		}
//		
////		if(callMachine.getIsEvent()) {
////			eventPalaver = Palaver.findByUuid(callMachine.getEventUuid());
////			if((eventPalaver == null) || (!callMachine.getActivePalaver().contains(eventPalaver))) {
////				palaver = Palaver.findByPalaverType(PalaverType.EVENT.toString());
////				newUuid = uiManipulation.copyPalaver(palaver.uuid);
////				callMachine.setEventUuid(newUuid);
////				uiManipulation.startPalaver(newUuid, Mode.MANUAL);
////				
////			}
////			callMachine.setIsEvent(false);
////		}
////		callMachine.getConfbridgeAbonentList(conference);
////		
////		eventPalaver = null;
////		palaver = null;
////		newUuid = null;
//		
//		timelinePalavers = null;
//		calendar = null;
//		//nowTime = null;
//		
//	}
}
