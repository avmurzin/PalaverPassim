package com.avmurzin.palaverpassim.db

import com.avmurzin.palaverpassim.global.PalaverType
import java.util.UUID;
/**
 * Встреча (набор абонентов, привязанный к номеру конференции и запланированными
 * временами начала и окончания).
 * @author Andrei Murzin (http://avmurzin.com)
 *
 */
class Palaver {
	UUID uuid;
	String description;
	long startTimestamp; //время начала встречи
	long stopTimestamp;  //время завершения встречи
	String palaverType; //тип палавера (обычный, шаблон, готовый для запуска)
	
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
