import java.util.UUID;

import com.avmurzin.palaverpassim.db.Abonent
import com.avmurzin.palaverpassim.db.Conference
import com.avmurzin.palaverpassim.db.Group
import com.avmurzin.palaverpassim.db.Palaver
import com.avmurzin.palaverpassim.db.Phone
import com.avmurzin.palaverpassim.sec.User
import com.avmurzin.palaverpassim.global.PalaverType

import org.apache.shiro.crypto.hash.Sha256Hash

class BootStrap {

	def init = { servletContext ->

		//тестовый пользователь
		def user = new User(username: "admin", passwordHash: new Sha256Hash("password").toHex())
		user.addToPermissions("palaver:*:*")
		user.save()

		//очистка таблиц абонентов, групп, комнат, палаверов
		Abonent abonent
		Abonent.findAll().each {it.delete()}

		Group group
		Group.findAll().each {it.delete()}

		Conference conference
		Conference.findAll().each {it.delete()}

		Palaver palaver
		Palaver.findAll().each {it.delete()}

		//тестовый набор абонентов
		def abonentData = [[fName: "Лазарь", mName: "Борисович", lName: "Пушистиков", description: "директор", address: "Москва", email: "avmurzin@gmail.com"],
			[fName: "Константин", mName: "Константинович", lName: "Константинов", description: "инопланетянин", address: "Антарес", email: "avmurzin@gmail.com"],
			[fName: "Лавр", mName: "Федотович", lName: "Вунюков", description: "председатель", address: "Китежград", email: "avmurzin@gmail.com"],
			[fName: "Рудольф", mName: "Архипович", lName: "Хлебовводов", description: "активист", address: "Тьмускорпионь", email: "avmurzin@gmail.com"],
			[fName: "Амвросий", mName: "Амбруазович", lName: "Выбегалло", description: "профессор", address: "НИИЧАВО", email: "avmurzin@gmail.com"],
			[fName: "Клоп", mName: '-', lName: "Говорун", description: "клоп", address: "спичечный коробок", email: "avmurzin@gmail.com"]]
		abonentData.each { data ->
			data << [uuid: UUID.randomUUID()]
			abonent = new Abonent(data)
			abonent.save(failOnError: true, flush: true)
			
			def ph = "${Math.round(Math.random()*100)}-${Math.round(Math.random()*100)}-${Math.round(Math.random()*100)}"
			if(abonent.lName.equals("Вунюков")) {
				ph = "9513559"
			}
			if(abonent.lName.equals("Хлебовводов")) {
				ph = "989086467383"
			}
			if(abonent.lName.equals("Выбегалло")) {
				ph = "1300"
			}

			def phone = new Phone(uuid: UUID.randomUUID(),
			//phoneNumber: "${Math.round(Math.random()*100)}-${Math.round(Math.random()*100)}-${Math.round(Math.random()*100)}",
			phoneNumber: "${ph}",
			description: "рабочий")
			phone.abonent = abonent
			phone.save(failOnError: true, flush: true)
		}

		//тестовый набор групп
		group = new Group(uuid: UUID.randomUUID(), description: "Тройка")
		group.abonent = Abonent.findAll("from Abonent as a where a.lName in (:names)",
				[names: ['Вунюков','Хлебовводов','Выбегалло']])
		group.save(failOnError: true, flush: true)
		group = new Group(uuid: UUID.randomUUID(), description: "Народ")
		group.abonent = Abonent.findAll("from Abonent as a where a.lName in (:names)",
				[names: ['Константинов','Говорун']])
		group.save(failOnError: true, flush: true)

		//тестовый набор конференций
		conference = new Conference(uuid: UUID.randomUUID(), description: "One",
		//phoneNumber: "${Math.round(Math.random()*100)}-${Math.round(Math.random()*100)}-${Math.round(Math.random()*100)}")
		phoneNumber: "2900")
		conference.save(failOnError: true, flush: true)

		conference = new Conference(uuid: UUID.randomUUID(), description: "Two",
		//phoneNumber: "${Math.round(Math.random()*100)}-${Math.round(Math.random()*100)}-${Math.round(Math.random()*100)}")
		phoneNumber: "2910")
		conference.save(failOnError: true, flush: true)

		conference = new Conference(uuid: UUID.randomUUID(), description: "Three",
		//phoneNumber: "${Math.round(Math.random()*100)}-${Math.round(Math.random()*100)}-${Math.round(Math.random()*100)}")
		phoneNumber: "2911")
		conference.save(failOnError: true, flush: true)

		//тестовый набор палаверов
		Calendar calendar = new GregorianCalendar()
		long nowTime = calendar.getTimeInMillis() / 1000
		long hour = 3600
		
		//palaver = new Palaver(uuid: UUID.randomUUID(), description: "Говорилка №1",
		palaver = new Palaver(uuid: UUID.fromString("ca4356df-4a2b-4df2-8a5f-dfc8b0294576"), description: "Говорилка №1",
		startTimestamp: nowTime + hour*2, stopTimestamp: nowTime + hour*3, palaverType: PalaverType.PREPARED.toString())
		palaver.conference = Conference.find("from Conference as c where c.description=:desc",
				[desc: "One"])
		palaver.abonent = Abonent.findAll("from Abonent as a where a.lName in (:names)",
				[names: ['Вунюков','Хлебовводов','Выбегалло']])
		palaver.save(failOnError: true, flush: true)

		palaver = new Palaver(uuid: UUID.randomUUID(), description: "Говорилка №2",
		startTimestamp: nowTime + hour*2, stopTimestamp: nowTime + hour*3, palaverType: PalaverType.PREPARED.toString())
		palaver.conference = Conference.find("from Conference as c where c.description=:desc",
				[desc: "Two"])
		palaver.abonent = Abonent.findAll("from Abonent as a where a.lName in (:names)",
				[names: ['Константинов','Говорун']])
		palaver.save(failOnError: true, flush: true)

		palaver = new Palaver(uuid: UUID.randomUUID(), description: "Говорилка №3",
		startTimestamp: nowTime + hour*6, stopTimestamp: nowTime + hour*7, palaverType: PalaverType.TEMPLATE.toString())
		palaver.conference = Conference.find("from Conference as c where c.description=:desc",
				[desc: "Two"])
		palaver.abonent = Abonent.findAll("from Abonent as a where a.lName in (:names)",
				[names: ['Константинов','Говорун']])
		palaver.save(failOnError: true, flush: true)

	}
	def destroy = {
	}
}
