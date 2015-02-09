class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
		
		"/" (controller: "palaver", action: "index")
		
		//получить таймзону
		"/timezone"(controller: "palaver", action: "getTimezone", method: "GET")
		
		//создать сессию (запланировать в расписании)
		"/palaver/$description/$startTimestamp/$stopTimestamp"(controller: "palaver", action: "createPalaver", method: "POST")
		
		//запустить сессию (начать обзвон по списку абонентов)
		"/palaver/$uuid/start"(controller: "palaver", action: "startPalaver", method: "POST")
		
		//остановить сессию (отключить по списку абонентов)
		"/palaver/$uuid/stop"(controller: "palaver", action: "stopPalaver", method: "POST")
		
		//получить сессию со списком абонентов и их статусами
		"/palaver/$uuid"(controller: "palaver", action: "getPalaver", method: "GET")
		
		//добавить абонента
		//передается JSON объект, соотв. доменному классу Abonent
		"/abonent"(controller: "palaver", action: "createAbonent", method: "POST")
		
		//TODO: удалить. Получить все uuid палаверов
		"/palaver/uuid"(controller: "palaver", action: "getAllPalaverUuid", method: "GET")
		
        //"/"(view:"/index")
        "500"(view:'/error')
	}
}
