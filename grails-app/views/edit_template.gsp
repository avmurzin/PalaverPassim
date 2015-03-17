<!DOCTYPE html>
<html>
    <head>
     <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <link rel="stylesheet" href="codebase/webix.css" type="text/css" media="screen" charset="utf-8">
        <g:javascript src="codebase/webix.js" />
        <style>

            .blue.webix_menu-x{
                background:#3498DB;
            }
        </style>
        
        <title>Управление телефонными конференциями</title>
    </head>
    <style type="text/css">
        .mark{
            width:100px;
            text-align: center;
            font-weight:bold;
            float:right;
            background-color:#777;
            color:white;
            border-radius:3px;
        }
        .info{
            width:100px;
            text-align: center;
            font-weight:normal;
            float:right;
            background-color:#F99;
            color:white;
            border-radius:3px;
        }
        .label{
            text-align: center;
            font-size: 15pt;
            font-weight:normal;
            background-color:#A77;
            color:white;
            border-radius:3px;
            margin: 10px;
        }
        .label_light{
            text-align: center;
            font-size: 15pt;
            font-weight:normal;
            background-color:#CCC;
            color:white;
            border-radius:3px;
            margin: 10px;
        }
    </style>
    
    <body>
<g:javascript>

palaver_uuid = "${uuid}";

//содержимое главного меню
var menu_data = [
    { id:"1",value:"На главную"},
    {id:"4",value:"Назад" }
];

//обработка меню
function menuSelect(id) {
    switch(id) {
    case "1":
        window.location.href = "index.html";
         break;
    case "4":
       window.history.back(); 
         break;
    default:
}
};


//объект главного меню
var menu = {
    view:"menu",
    id: "top_menu",
    data: menu_data,
    css:"blue",
    type:{
        subsign:true,

    },
    on:{
        onMenuItemClick:function(id){
            menuSelect(id);
        }
        }
};


//тулбар справа от главного меню
var toolbar = {
        view:"toolbar", paddingY:0,  elements:[
            {}, 
            { view:"label", id:"logged_user", value: "", align: "right"},
            { view:"button", label:"Выйти", width:100, on:{
                onItemClick:function(){
                    logout();
                }
                } }
        ]
    };
    
//дерево контейнеров (нижний блок)
var container_tree = {
    view : "tree",
    id : "container_tree",
    disabled: true,
    select : false,
    type:"lineTree",
    template:"{common.icon()} <img src='images/#image#.png' style='float:left; margin:3px 4px 0px 1px;'> <span>#value#</span>",
    data : [{id: "123", value: "Требуется залогиниться", image: "group"}],
    on:{
        onItemClick:function(id){
            //palaver_properties(id);
        }
       }
};    

//список абонентов палавера
//
var abonent_list = {
        view: "list",
        id: "abonent_list",
        template: "<strong>#description#</strong>",
       select:false,
        data:[]
}


//форма приглашения дополнительного абонента
var form_addabonent = {
        view:"form", 
        id:"form_addabonent",
        width: "auto",
        elements:[{view:"fieldset", label:"Добавить участника в конференцию", body:{rows:[
            { view:"text", name:"name", label:"Имя: ", placeholder: "Начинайте вводить буквы для поиска", on:{'onTimedKeyPress': function(){ onNameChange(); }
             }},
            { view:"text", name:"phone", label:"Телефон: ", placeholder: "Начинайте вводить цифры для поиска", on:{'onTimedKeyPress': function(){ onPhoneChange(); }
             }},
            { margin:5, view:"button", value:"Добавить", on : {
                onItemClick:function(){
                    add_abonent();
                }
                }}
       ] }}]
        }

//нижний лист найденных абонентов (обновляется по мере ввода данных в форму приглашения дополнительного абонента)
var search_list = {
        view: "list",
        id: "search_list",
        template: "<strong>#description#</strong> #phone#",
       select:true,
        data:[],
        on:{
        onItemClick:function(id){
            select_abonent(id, this.getItem(id).description, this.getItem(id).phone);
        }
       }
}

//переменная хранит id выбранного из списка поиска абонента
var abonent_id = '';

//форма создания палавера
var create_palaver = {
		view:"form", id: "palaver_form", scroll:false, elements:[
                                      { view:"text", label:"Описание", name:"description", value:"Новая встреча", width:400},
                                      {view:"select", id:"conference_select", name:"conference" ,label:"Комната", options:"conference", labelAlign: 'left'},
                                      {view:"select", id:"type_select", name:"type" ,label:"Тип", value: "TEMPLATE", options: [{id:"TEMPLATE", value: "Шаблон"}, {id:"PREPARED", value: "Стандартные мероприятия"}], labelAlign: 'left', on:{
                                          onChange:function(){
                                             
                                          }
                                          }},

                                          { margin:5, cols:[
                                                            { view:"button", type:"form", value:"Сохранить", click:function(){
                                                                updatePalaver();
                                                                }},
                                                            { view:"button", value:"Удалить", type:"danger", click:function(){
                                                                deletePalaver();
                                                            } }
                                                        ]}
                                      
		                                 ]
		
}


//верстка страницы интерфейса
webix.ui({
    rows:[
        {type:"clean", cols: [menu,toolbar]},
        { cols: [{rows: [{view: "label", label:"<span class='label'>Параметры конференции</span>"}, 
                         create_palaver, {view: "label", label:"<span class='label_light'>Ранее запланированные на тот же период встречи</span>"}, 
                         container_tree, {}]}, 
                 {view:"resizer"}, 
                 {rows: [{view: "label", label:"<span class='label'>Участники конференции</span>"},
                         abonent_list, form_addabonent, search_list, {}]}]}
    ]
});

//создание палавера
function updatePalaver() {
	if((($$('palaver_form').getValues().start == null) || ($$('palaver_form').getValues().end == null)) && ($$('palaver_form').getValues().type == "NORMAL")) {
		webix.alert("Заполнены не все поля");
		return;
	}
	
	if($$('palaver_form').getValues().type != "NORMAL") {
	   var startTime = 0;
	   var stopTime = 0;
	} else {
	    var startTime = $$('palaver_form').getValues().start.getTime() / 1000;
        var stopTime = $$('palaver_form').getValues().end.getTime() / 1000;
	}
	
	var desc = $$('palaver_form').getValues().description;
	var id = $$('palaver_form').getValues().conference;
	var palaverType = $$('palaver_form').getValues().type;
	var palaver = {
			startTimestamp: startTime,
			stopTimestamp: stopTime,
			description: desc,
			conferenceUuid: id,
			palaverType: palaverType
	};
    webix.ajax().headers({"Content-type":"application/json"}).post("palaver/"+palaver_uuid, 
            JSON.stringify(palaver), 
            function(text, data) {
        if (data.json().result == false) {
            webix.alert(data.json().message);
        } else {
        webix.message("Сохранено");
        	update_form();
        	
        }
    });
};

//удаление палавера
function deletePalaver() {
	webix.ajax().del("palaver/" + palaver_uuid, {}, function(text, data) {
        if (data.json().result == false) {
            webix.alert(data.json().message);
            update_abonent_list(); 
        } else {
        	//window.location.href = "select_template.html";
        	window.history.back(); 
        }
    }); 	
}

//функция поиска по текстовой части абонента
function onNameChange() {
    abonent_id = '';
    $$('form_addabonent').setValues({ phone:"" }, true);
    var text = $$('form_addabonent').getValues().name;
    webix.ajax("abonent/findByText/" + text, function(text, data) {
        $$("search_list").clearAll();
        $$("search_list").parse(data.json());
        $$("search_list").sort('#description#', 'asc');
    });
}

//функция поиска по телефонной (цифровой) части абонента
function onPhoneChange() {
    abonent_id = '';
    $$('form_addabonent').setValues({ name:"" }, true);
    var text = $$('form_addabonent').getValues().phone;
    webix.ajax("abonent/findByPhone/" + text, function(text, data) {
        $$("search_list").clearAll();
        $$("search_list").parse(data.json());
        $$("search_list").sort('#description#', 'asc');
    });
}

//функция обновления формы приглашения по выбранной позиции в нижнем листе.
function select_abonent(id, description, phone) {
        abonent_id = id;
        $$("form_addabonent").setValues({name: description, phone: phone});
        $$("search_list").clearAll();
}

//Функция добавления абонента в палавер. Если абонент выбран из списка, то передается id,
//если нет, то вызывается функция создания нового абонента и далее подключение его к палаверу.
function add_abonent() {
     if(abonent_id == '') {
         //webix.message("No id");
         new_abonent();
     } else {
    		 webix.ajax().post("palaver/" + palaver_uuid + "/abonent/" + abonent_id, {}, function(text, data) {
             if (data.json().result == false) {
                 webix.alert(data.json().message);
                 update_abonent_list() 
             } else {
            	 update_abonent_list() 
             }
         }); 
     }
}

//-----------добавить нового абонента
    function new_abonent() {

            new_abonent_form = {
                    view : "form", 
                    id : "new_abonent_form", 
                    elements : [ {view : "text", label : 'Имя', name : "fName", placeholder : "Иван"},
                                 {view : "text", label : 'Отчество', name : "mName", placeholder : "Иванович"}, 
                                 {view : "text", label : 'Фамилия', name : "lName", placeholder : "Рабинович"},
                                 {view : "text", label : 'Описание', name : "description", placeholder : "дворник"}, 
                                 {view : "text", label : 'Адрес', name : "address", placeholder : "Лондон, Тауэр"},
                                 {view : "text", label : 'E-mail', name : "email", type: "email", placeholder : "email@fsb.ru"},
                                 {view : "text", label : 'Телефон', name : "phone", placeholder : "13-13-13"},
                                 {view : "button", value : "Создать", on : {
                                     onItemClick:function(){
                                         make_abonent();
                                     }
                                     }
                    } ]
                };
            
            //окно добавления нового абонента
            webix.ui({
                view : "window",
                position:"center",
                id : "new_abonent",
                head : "<i>Абонент не найден. Создать.</i>",
                body : {
                    rows : [ new_abonent_form, {
                        view : "button",
                        id: "cancel1",
                        label : "Отменить",
                        click : ("$$('new_abonent').hide();")
                    } ]
                }
            });
            
            $$('new_abonent').show();
 
        
    };
//добавить нового абонента, продолжение
function make_abonent() {
        var fName = $$('new_abonent_form').getValues().fName;
        var mName = $$('new_abonent_form').getValues().mName;
        var lName = $$('new_abonent_form').getValues().lName;
        var description = $$('new_abonent_form').getValues().description;
        var address = $$('new_abonent_form').getValues().address;
        var email = $$('new_abonent_form').getValues().email;
        var phone = $$('new_abonent_form').getValues().phone;
        
        if(phone == '') {
            webix.alert("Безобразие! Укажите хотя бы телефон!");
            return;
        }
        
    var abonent = {
            fName: fName,
            mName: mName,
            lName: lName,
            description: description,
            address: address,
            email: email,
            phone: phone
    };
    webix.ajax().headers({"Content-type":"application/json"}).post("abonent", 
            JSON.stringify(abonent), 
            function(text, data) {
        if (data.json().result == false) {
            webix.alert(data.json().message);
        } else {
        
            webix.ajax().post("palaver/" + palaver_uuid + "/abonent/" + data.json().message, {}, function(text, data) {
             if (data.json().result == false) {
                 webix.alert(data.json().message);
                 update_abonent_list() 
             } else {
                 update_abonent_list() 
             }
         });
            //select_abonent(data.json().message, fName + " " + mName + " " + lName + "(" +description +")", phone);
            $$('new_abonent').hide();
        }
    });
        
        
}
//-----------------

//обновление дерева ранее запланированных на то же время
function update_tree() {
    $$("container_tree").clearAll();
    webix.ajax("palaver/timeline", function(text, data) {
    	
        $$("container_tree").parse(data.json());
        $$("container_tree").openAll();
        $$("container_tree").refresh();
        $$("container_tree").sort('#startTimestamp#', 'asc');

    });
}

//обновление списка абонентов
function update_abonent_list() {
    webix.ajax("palaver/" + palaver_uuid, function(text, data) {
        if(data.json().description == '') {
            
        } else {
            $$("abonent_list").clearAll();
            $$("abonent_list").parse(data.json().items);
            $$("abonent_list").sort('#description#', 'asc');
        }
    });	
}

//окно загрузилось
webix.attachEvent("onReady", function(){
	update_tree();
	
	webix.ajax("palaver/" + palaver_uuid, function(text, data) {
	    if(data.json().description == '') {
	    	
	    } else {
  	
	    	$$('palaver_form').setValues({ description: data.json().description}, true);
	        $$("abonent_list").clearAll();
	        $$("abonent_list").parse(data.json().items);
	        $$("abonent_list").sort('#description#', 'asc');
	    }

	    
	});
	 
});

function logout() {
    webix.message("Выход")
    window.location.assign("auth/signOut");
}
</g:javascript>
    </body>
</html>