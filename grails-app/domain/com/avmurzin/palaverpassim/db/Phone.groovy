package com.avmurzin.palaverpassim.db

import java.util.UUID;

class Phone {
	UUID uuid;
	String phoneNumber;
	String digitalPhoneNumber;
	String description;
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		this.digitalPhoneNumber = (phoneNumber =~ /\D/).replaceAll("");
	}
	
	static mapping = {
		table "phone";
		uuid column: "uuid", length: 16;
		phoneNumber column: "phonenumber";
		digitalPhoneNumber column: "digitalphonenumber";
		description column: "description", length: 400;
	}
	
	static belongsTo = [abonent : Abonent];

    static constraints = {
    }
}
