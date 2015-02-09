package com.avmurzin.palaverpassim.db

import java.util.UUID;

class Group {

	UUID uuid;
	String description;

	static mapping = {
		table "abonentgroup";
		uuid column: "uuid", length: 16;
		description column: "description", length: 400;
	}
	
	static hasMany = [abonent:Abonent]
	
	static constraints = {
	}
}
