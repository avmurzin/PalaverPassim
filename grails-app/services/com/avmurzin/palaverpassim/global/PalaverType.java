package com.avmurzin.palaverpassim.global;

/**
 * Тип palaver - обычный (т.е. собственно конферецния), шаблон (как база для
 * создания нового), типовой (для немедленного старта).
 * @author Andrei Murzin (http://avmurzin.com)
 *
 */
public enum PalaverType {
//	NORMAL("NORMAL"),
//	TEMPLATE("TEMPLATE"),
//	PREPARED("PREPARED");
//	
//	final String value;
//	
//	PalaverType(String value) {
//		this.value = value;
//	}
//	@Override
//	public String toString() { return value; }
//	
//	public String getKey() { return name(); }
	NORMAL(""),
	TEMPLATE("Шаблоны"),
	PREPARED("Стандартные мероприятия (готовы для запуска)"),
	EMPTY("Пустой шаблон"),
	EVENT("Запуск по событию");
	
	String description;
	
	PalaverType(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
