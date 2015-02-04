package com.avmurzin.palaverpassim.db

import java.util.UUID;

class Palaver {
	UUID uuid;
	String description;
	long startTimestamp; //время начала встречи
	long stopTimestamp;  //время завершения встречи
	
	static mapping = {
		table "palaver";
		uuid column: "uuid", length: 16;
		description column: "description", length: 400;
	}
	
	static hasMany = [abonent : Abonent]
	static belongsTo = [conference : Conference];
	
    static constraints = {
    }
}
