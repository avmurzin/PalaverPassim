package com.avmurzin.palaverpassim.db

import java.util.UUID;

class Phone {
	UUID uuid;
	String phoneNumber;
	String description;
	
	static mapping = {
		table "phone";
		uuid column: "uuid", length: 16;
		phoneNumber column: "phonenumber";
		description column: "description", length: 400;
	}
	
	static belongsTo = [abonent : Abonent];

    static constraints = {
    }
}
