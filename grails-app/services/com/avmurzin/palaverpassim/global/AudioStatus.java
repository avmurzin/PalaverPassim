package com.avmurzin.palaverpassim.global;

public enum AudioStatus {
	MUTED("to_sound_off.png", "mutedto", "mic_off.png", "mutedfrom"),
	UNMUTED("to_sound_on.png", "unmutedto", "mic_on.png", "unmutedfrom");
	
	private String iconNameTo;
	private String styleNameTo;
	private String iconNameFrom;
	private String styleNameFrom;
	
	AudioStatus(String iconNameTo, String styleNameTo, String iconNameFrom, String styleNameFrom){
		this.iconNameTo = iconNameTo;
		this.styleNameTo = styleNameTo;
		this.iconNameFrom = iconNameFrom;
		this.styleNameFrom = styleNameFrom;
	}
	
	String getIconNameTo() {
		return iconNameTo;
	}
	
	String getStyleNameTo() {
		return styleNameTo;
	}
	
	String getIconNameFrom() {
		return iconNameFrom;
	}
	
	String getStyleNameFrom() {
		return styleNameFrom;
	}
}
