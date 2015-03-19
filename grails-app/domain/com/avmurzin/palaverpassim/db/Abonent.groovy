package com.avmurzin.palaverpassim.db
/**
 * Участник встречи (с номерами телефонов).
 * @author Andrei Murzin (http://avmurzin.com)
 *
 */
class Abonent {
	UUID uuid;
	String fName;
	String mName;
	String lName;
	String description;
	String address;
	String email;

	static mapping = {
		table "abonent";
		uuid column: "uuid", length: 16;
		description column: "description", length: 400;
		fName column: "fName", length: 200;
		mName column: "mName", length: 200;
		lName column: "lName", length: 200;
		address column: "address", length: 400;
		email column: "email";
	}
	
	static hasMany = [phones:Phone]
	
	static constraints = {
		//email email: true, blank: true
		email blank: true
	}
}
