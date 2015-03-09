class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
		
		"/" (controller: "palaver", action: "index")
		
		"/nks"(controller: "palaver", action: "nks")
		
		//получить таймзону
		"/timezone"(controller: "palaver", action: "getTimezone", method: "GET")
		
		//создать сессию (запланировать в расписании)
		"/palaver/$description/$startTimestamp/$startTimestamp"(controller: "palaver", action: "createPalaver", method: "POST")
		
		//запустить сессию (начать обзвон по списку абонентов)
		"/palaver/$uuid/start"(controller: "palaver", action: "startPalaver", method: "POST")
		
		//остановить сессию (отключить по списку абонентов)
		"/palaver/$uuid/stop"(controller: "palaver", action: "stopPalaver", method: "POST")
		
		//получить сессию со списком абонентов и их статусами
		"/palaver/$uuid"(controller: "palaver", action: "getPalaver", method: "GET")
		
		//добавить абонента
		//передается JSON объект, соотв. доменному классу Abonent
		"/abonent"(controller: "palaver", action: "createAbonent", method: "POST")
		
		//получить список палаверов за указанный период (по умолчанию текущие сутки)
		//?startTimestamp=&stopTimestamp=
		//возвращает JSON объект, соотв. таблице
		"/palaver/timeline"(controller: "palaver", action: "getPalaverTimeline", method: "GET")
		
		//Получить список конференций (комнат).
		"/conference"(controller: "palaver", action: "getConference", method: "GET")
		
		//переключалка звука абонента
		//$todo = [muteto, unmuteto, mutefrom, unmutefrom]
		"/abonent/$uuid/$todo/$palaveruuid"(controller: "palaver", action: "setAbonentAudio", method: "POST")
		
		//отключить абонента от палавера
		"/palaver/$palaveruuid/abonent/$uuid/kick"(controller: "palaver", action: "kickAbonentFromPalaver", method: "GET")
		
		//подключить абонента к палаверу
		"/palaver/$palaveruuid/abonent/$uuid/connect"(controller: "palaver", action: "connectAbonentToPalaver", method: "GET")
		
		//получить дерево шаблонов палаверов
		//возвращает JSON-дерево для формирования интерфейса выбора шаблона палавера.
		"/interface/template"(controller: "palaver", action: "getPalaverTemplates", method: "GET")
		
		//редирект на указанные страницы с параметром uuid
		//?page=&uuid=
		"/redirect"(controller: "palaver", action: "getPage", method: "GET")
		
		//TODO: удалить. Получить все uuid палаверов
		"/palaver/uuid"(controller: "palaver", action: "getAllPalaverUuid", method: "GET")
		
		//TODO: удалить. Тест астериска.
		"/asterisk"(controller: "palaver", action: "asterisk", method: "GET")
		
        //"/"(view:"/index")
        "500"(view:'/error')
	}
}
