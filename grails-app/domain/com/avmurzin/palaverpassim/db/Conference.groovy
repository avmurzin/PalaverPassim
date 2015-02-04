package com.avmurzin.palaverpassim.db

import java.util.UUID;
/**
 * Пул доступных конференций (серверный ресурс).
 * @author Andrei Murzin (http://avmurzin.com)
 *
 */
class Conference {
	UUID uuid;
	String phoneNumber;
	String description;
	
	static mapping = {
		table "conference";
		uuid column: "uuid", length: 16;
		phoneNumber column: "phonenumber";
		description column: "description", length: 400;
	}
	
	static hasMany = [palaver : Palaver]
	
    static constraints = {
    }
}
