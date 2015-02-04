package com.avmurzin.palaverpassim.db
/**
 * 
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

	static mapping = {
		table "abonent";
		uuid column: "uuid", length: 16;
		description column: "description", length: 400;
		fName column: "fName", length: 50;
		mName column: "mName", length: 50;
		lName column: "lName", length: 50;
		address column: "address", length: 400;
	}
	
	static hasMany = [phones:Phone]
	
	static constraints = {
	}
}
