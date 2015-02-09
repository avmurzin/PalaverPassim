package com.avmurzin.palaverpassim.global;

public enum UserRole {
	ROOT, //неограниченные права в системе
	CONF_OWNER, //владелец конференции
	CONF_MEMBER, //участник конференции
	WEBUSER; //доступ к веб-интерфейсу
	
	UserRole(){}
}
