package com.avmurzin.palaverpassim.global;

public enum AbonentStatus {
	BUSY ("", ""),
	DISCONNECTED ("phone_off.png", "disconnected"),
	CONNECTED("phone_on.png", "connected");

	private String iconName;
	private String styleName;
	
	AbonentStatus(String iconName, String styleName){
		this.iconName = iconName;
		this.styleName = styleName;
	}
	
	String getIconName() {
		return iconName;
	}
	
	String getStyleName() {
		return styleName;
	}
}
