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
     {id:"4",value:"Назад" }
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
 
//блок списка абонентов конференции с кнопками управления
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
            //webix.message("Абонент будет подключен, ожидайте");
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

//верстка страницы интерфейса
webix.ui({
    rows:[
        {type:"clean", cols: [menu,toolbar]},
        palaver_title,
        abonents_list,
        form_addabonent,
        search_list
        
    ]
});
 
//обработка меню
function menuSelect(id) {
    switch(id) {
    case "4":
        go_back();
         break;
    default:
}
};

var palaverUuid = "${uuid}";

function start_palaver() {
    webix.ajax().post("palaver/" + palaverUuid + "/start", {}, function(text, data) {
        if (data.json().result == false) {
            webix.alert(data.json().message);
        } else {
        	//webix.alert(data.json().message);
        	palaverUuid = data.json().message;
        	$$("top_menu").hideItem("1");
        	$$("top_menu").showItem("2");
        	refresh_timeout();
        }
    });
}

function stop_palaver() {
    webix.ajax().post("palaver/" + palaverUuid + "/stop", {}, function(text, data) {
        if (data.json().result == false) {
            webix.alert(data.json().message);
        } else {
            $$("top_menu").hideItem("2");
            $$("top_menu").hideItem("1");
            $$("top_menu").showItem("3");
            $$('form_addabonent').disable();
            $$("abonents").disable();
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
    if (data.json().result == false) {
            webix.alert(data.json().message);
    }
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
// если нет, то вызывается функция создания нового абонента и далее подключение его к палаверу.
function add_abonent() {
        if(abonent_id == '') {
            //webix.message("No id");
            new_abonent();
        } else {
            webix.ajax("palaver/" + palaverUuid + "/abonent/" + abonent_id + "/connect", function(text, data) {
                if (data.json().result == false) {
                    webix.alert(data.json().message);
                    refresh();
                } else {
                    refresh();
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
        
            webix.ajax("palaver/" + palaverUuid + "/abonent/" + data.json().message + "/connect", function(text, data) {
                if (data.json().result == false) {
                    webix.alert(data.json().message);
                    refresh();
                } else {
                    refresh();
                }
            }); 
            //select_abonent(data.json().message, fName + " " + mName + " " + lName + "(" +description +")", phone);
            $$('new_abonent').hide();
        }
    });
        
        
}
//-----------------

function go_back() {
    window.history.back(); 
}


function refresh_timeout() {
    webix.ajax("palaver/" + palaverUuid, function(text, data) {
        $$("abonents").clearAll();
        $$("abonents").parse(data.json().items);
        palaverUuid = data.json().uuid;
        var title = "<span class='labeltext'>" + data.json().description + " </span>(Комната: " + data.json().conference + ")";
        $$("palaverTitle").setValue(title);
        //$$("form_addabonent").disable();
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
        //$$("form_addabonent").disable();
        $$("abonents").sort('#description#', 'asc');
    }); 
}

webix.attachEvent("onReady", function(){

  refresh_timeout();
});

function logout() {
    webix.message("Выход")
    window.location.assign("auth/signOut");
}
</g:javascript>
    </body>
</html>