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
		description column: "description", length: 400, defaultValue: 'абонент';
		fName column: "fName", length: 50, defaultValue: 'Имя';
		mName column: "mName", length: 50, defaultValue: 'Отчество';
		lName column: "lName", length: 50, defaultValue: 'Фамилия';
		address column: "address", length: 400, defaultValue: 'адрес';
		email column: "email";
	}
	
	static hasMany = [phones:Phone]
	
	static constraints = {
		email email: true, blank: true
	}
}
