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
            vertical-align: middle;
            width:60px;
            text-align: center;
            font-weight:bold;
            float:right;
            background-color:#FFF;
            color:white;
            border-radius:3px;
        }
        .info{
            width:60px;
            text-align: center;
            font-weight:normal;
            float:right;
            background-color:#F99;
            color:white;
            border-radius:3px;
        }
        .space{
            width:2px;
            border: solid 1px black;
            text-align: center;
            font-weight:normal;
            float:right;
            background-color:#FFF;
            color:white;
            border-radius:3px;
        }
        .inbox{
        vertical-align: middle;
        display:inline-block;
        }
        .labeltext{
            color: black; 
            font-size: 200%; 
            font-family: serif;
        }
        .disconnected{
            width:60px;
            text-align: center;
            font-weight:normal;
            float:right;
            background-color:#F99;
            color:white;
            border-radius:3px;
        }
        .connected{
            width:60px;
            text-align: center;
            font-weight:normal;
            float:right;
            background-color:#9F9;
            color:white;
            border-radius:3px;
        }
        .mutedto{
            width:60px;
            text-align: center;
            font-weight:normal;
            float:right;
            background-color:#F99;
            color:white;
            border-radius:3px;
        }
        .unmutedto{
            width:60px;
            text-align: center;
            font-weight:normal;
            float:right;
            background-color:#9F9;
            color:white;
            border-radius:3px;
        }
        .mutedfrom{
            width:60px;
            text-align: center;
            font-weight:normal;
            float:right;
            background-color:#F99;
            color:white;
            border-radius:3px;
        }
        .unmutedfrom{
            width:60px;
            text-align: center;
            font-weight:normal;
            float:right;
            background-color:#9F9;
            color:white;
            border-radius:3px;
        }
        .spinner{
            width:60px;
            text-align: center;
            font-weight:normal;
            float:right;
            background-color:#FFF;
            color:white;
            border-radius:3px;
        }
    </style>
    
    <body>

<g:javascript>
//содержимое главного меню
var menu_data = [
    { id:"1",value:"Управление конференцией", submenu:[
         {id: "1.1", value: "Начать"}, 
         {id: "1.2", value: "Завершить"}
    ]}
];

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
        	//webix.alert("click!");
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
 
//блок списка абонентов конференции
var abonents_list = {
        view: "list",
        id: "abonents",
        template: "<strong>#description#</strong> <div div class='space'>.</div><div class='#styleFrom#'><img src='images/#iconFrom#'></div><div div class='space'>.</div><div class='#styleTo#'><img src='images/#iconTo#'></div> <div div class='space'>.</div><div class='#styleAbonent#'><img src='images/#iconAbonent#'></div><div div class='space'>.</div>",
        type:{
            //height:40
        },
        select:false,
        onClick:{
            connected:function(e, id){
                webix.message("Абонент будет отключен, ожидайте");
                //del_user(this.getItem(id).username, this.getItem(id).role);
                kick_abonent(this.getItem(id).uuid);
                return false;
            },
           disconnected:function(e, id){
            webix.message("Абонент будет подключен, ожидайте");
            connect_abonent(this.getItem(id).uuid);
            return false;
           },
           mutedto:function(e, id){
               webix.alert("В текущей версии управление звуком в сторону абонента не поддерживается");
               return false;
           },
           unmutedto:function(e, id){
               webix.alert("В текущей версии управление звуком в сторону абонента не поддерживается");
               return false;
           },
           unmutedfrom:function(e, id){
               audioFromAbonent(this.getItem(id).uuid, "mutefrom");
               return false;
           },
           mutedfrom:function(e, id){
        	   audioFromAbonent(this.getItem(id).uuid, "unmutefrom");
               return false;
           }
        },
        data:[],
        on:{
            onItemClick:function(id){
            	//webix.message(this.getItem(id).uuid);
            }
           }
}

//заголовок конференции
var palaver_title = { view:"label", id: "palaverTitle", label: "Label",  align:"center"}

//форма приглашения дополнительного абонента
var form_addabonent = {
        view:"form", 
        id:"form_addabonent",
        width: "auto",
        elements:[{view:"fieldset", label:"Пригласить в конференцию", body:{rows:[
            { view:"text", name:"name", label:"Имя: "},
            { view:"text", name:"phone", label:"Телефон: "},
            { margin:5, view:"button", value:"Добавить", on : {
                onItemClick:function(){
                    add_abonent();
                }
                }}
       ] }}]
        }

//верстка страницы интерфейса
webix.ui({
    rows:[
        {type:"clean", cols: [menu,toolbar]},
        palaver_title,
        abonents_list,
        form_addabonent
        
    ]
});

//обработка меню
function menuSelect(id) {
	//webix.message(id);
    switch(id) {
    case "1.1":
        start_palaver();
        //$$("top_menu").showItem("1.2");
        //$$("top_menu").hideItem("1.1");
         break;
    case "1.2":
        stop_palaver();
        //$$("top_menu").showItem("1.1");
        //$$("top_menu").hideItem("1.2");
         break;
    default:
}
};

//var palaverUuid = "ca4356df-4a2b-4df2-8a5f-dfc8b0294576";
var palaverUuid = "${uuid}";

function start_palaver() {
    webix.ajax().post("palaver/" + palaverUuid + "/start", {}, function(text, data) {
        if (data.json().result == false) {
            webix.alert(data.json().message);
        } else {
        	webix.alert(data.json().message);
        	palaverUuid = data.json().message;
        	$$("top_menu").hideItem("1.1");
        	$$("top_menu").showItem("1.2");
        	refresh_timeout();
        }
    });
}

function stop_palaver() {
    webix.ajax().post("palaver/" + palaverUuid + "/stop", {}, function(text, data) {
        if (data.json().result == false) {
            webix.alert(data.json().message);
        } else {
            webix.alert(data.json().message);
        }
    });
}


function kick_abonent(uuid) {
	
    webix.ajax("palaver/" + palaverUuid + "/abonent/" + uuid + "/kick", function(text, data) {

    }); 	
}

function connect_abonent(uuid) {
	//webix.message("palaver/" + palaverUuid + "/abonent/" + uuid + "/connect");
    webix.ajax("palaver/" + palaverUuid + "/abonent/" + uuid + "/connect", function(text, data) {

    });     
}

function audioFromAbonent(uuid, todo) {
	//webix.message("abonent/" + uuid + "/" + todo + "/" + palaverUuid);
    webix.ajax().post("abonent/" + uuid + "/" + todo + "/" + palaverUuid, {}, function(text, data) {
        if (data.json().result == false) {
            webix.alert(data.json().message);
        } else {
            webix.alert(data.json().message);
        }
    });	
}

function refresh_timeout() {
    webix.ajax("palaver/" + palaverUuid, function(text, data) {
        $$("abonents").clearAll();
        $$("abonents").parse(data.json().items);
        palaverUuid = data.json().uuid;
        var title = "<span class='labeltext'>" + data.json().description + " </span>(Комната: " + data.json().conference + ")";
        $$("palaverTitle").setValue(title);
        $$("form_addabonent").disable();
        $$("abonents").sort('#description#', 'asc');
        setTimeout(refresh_timeout, 2000);
    });	
}

function refresh() {
    webix.ajax("palaver/" + palaverUuid, function(text, data) {
        $$("abonents").clearAll();
        $$("abonents").parse(data.json().items);
        palaverUuid = data.json().uuid;
        var title = "<span class='labeltext'>" + data.json().description + " </span>(Комната: " + data.json().conference + ")";
        $$("palaverTitle").setValue(title);
        $$("form_addabonent").disable();
        $$("abonents").sort('#description#', 'asc');
    }); 
}

webix.attachEvent("onReady", function(){
    $$("top_menu").hideItem("1.2");
  refresh();
});

function logout() {
    webix.message("Выход")
    window.location.assign("auth/signOut");
}
</g:javascript>
    </body>
</html>