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
		//передается JSON объект, соотв. доменному классу Palaver
		//и параметр uuid - номер комнаты
		//"/palaver/$uuid"(controller: "palaver", action: "createPalaver", method: "POST")
		//"/palaver/$description/$startTimestamp/$startTimestamp"(controller: "palaver", action: "createPalaver", method: "POST")
		
		//обновить палавер (передать новые свойства)
		//передается JSON объект, соотв. доменному классу Palaver
		"/palaver/$uuid"(controller: "palaver", action: "updatePalaver", method: "POST")
		
		//получить сессию со списком абонентов и их статусами
		"/palaver/$uuid"(controller: "palaver", action: "getPalaver", method: "GET")
		
		//удалить сессию
		"/palaver/$uuid"(controller: "palaver", action: "deletePalaver", method: "DELETE")
		
		//запустить сессию (начать обзвон по списку абонентов)
		"/palaver/$uuid/start"(controller: "palaver", action: "startPalaver", method: "POST")
		
		//остановить сессию (отключить по списку абонентов)
		"/palaver/$uuid/stop"(controller: "palaver", action: "stopPalaver", method: "POST")
		
		//добавить абонента
		//передается JSON объект, соотв. доменному классу Abonent
		"/abonent"(controller: "palaver", action: "createAbonent", method: "POST")
		
		//получить список палаверов за указанный период (по умолчанию текущие сутки)
		//?startTimestamp=&stopTimestamp=
		//возвращает JSON объект, соотв. таблице
		"/palaver/timeline"(controller: "palaver", action: "getPalaverTimeline", method: "GET")
		
		//получить список всех активных палаверов на текущий момент
		"/palaver/active"(controller: "palaver", action: "getActivePalaver", method: "GET")
		
		//Получить список конференций (комнат).
		"/conference"(controller: "palaver", action: "getConference", method: "GET")
		
		//переключалка звука абонента
		//$todo = [muteto, unmuteto, mutefrom, unmutefrom]
		"/abonent/$uuid/$todo/$palaveruuid"(controller: "palaver", action: "setAbonentAudio", method: "POST")
		
		//отключить абонента от палавера
		"/palaver/$palaveruuid/abonent/$uuid/kick"(controller: "palaver", action: "kickAbonentFromPalaver", method: "GET")
		
		//добавить абонента к палаверу
		"/palaver/$palaveruuid/abonent/$uuid"(controller: "palaver", action: "addAbonentToPalaver", method: "POST")
		
		//подключить абонента к палаверу
		"/palaver/$palaveruuid/abonent/$uuid/connect"(controller: "palaver", action: "connectAbonentToPalaver", method: "GET")
		
		//получить дерево шаблонов палаверов
		//возвращает JSON-дерево для формирования интерфейса выбора шаблона палавера.
		"/interface/template"(controller: "palaver", action: "getPalaverTemplates", method: "GET")
		
		//редирект на указанные страницы с параметром uuid
		//?page=&uuid=
		"/redirect"(controller: "palaver", action: "getPage", method: "GET")
	
		
		// Поиск абонента по нескольким введенным символам. Искать по любому текстовому полю или номеру телефона.
		"/abonent/findByPhone/$phone"(controller: "palaver", action: "findAbonentByPhone", method: "GET")
		"/abonent/findByText/$text"(controller: "palaver", action: "findAbonentByText", method: "GET")
		
		//TODO: удалить. Получить все uuid палаверов
		"/palaver/uuid"(controller: "palaver", action: "getAllPalaverUuid", method: "GET")
		
		//TODO: удалить. Тест астериска.
		"/asterisk"(controller: "palaver", action: "asterisk", method: "GET")
		
        //"/"(view:"/index")
        "500"(view:'/error')
	}
}
