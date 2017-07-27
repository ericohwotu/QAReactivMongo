package controllers

import javax.inject.Inject

import models.Item
import play.api.i18n.{I18nSupport, MessagesApi}

import scala.util.{Failure, Success}
import scala.concurrent.Future
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json.collection._
import reactivemongo.play.json._
import reactivemongo.play.json.collection.{JSONCollection, JsCursor}
import JsCursor._
import helpers.{MongoHelpers, NavbarHelpers}
import play.api.libs.json._
import reactivemongo.api._


class Application @Inject()
(val messagesApi: MessagesApi, val reactiveMongoApi: ReactiveMongoApi, val navbarHelpers: NavbarHelpers,
 val mongoHelpers: MongoHelpers) extends Controller
  with MongoController with ReactiveMongoComponents with I18nSupport {

  //the collection for the items
  def itemCol: Future[JSONCollection] = database.map(_.collection[JSONCollection]("ItemsCollection"))


  def index(id: Int=0) = Action {
    Ok (views.html.home(navbarHelpers.homePage, Item.items, id)(Item.itemForm))
  }



  def add = Action.async {
    val result = itemCol.flatMap(_.insert(Item.items.head))
    result.map(_ => Ok("worked"))
  }

  def find(name: String) = Action.async {
    val cursor: Future[Cursor[Item]] = itemCol.map{_.find(Json.obj("name"->name)).
      cursor[Item](ReadPreference.primary)}

    val list: Future[List[Item]] = cursor.flatMap(_.collect[List]())

    list.map{
      item =>
        Ok(item.toString())
    }
  }

  def getAll = Action.async {
    val cursor: Future[Cursor[Item]] = itemCol.map{_.find(Json.obj())
      .sort(Json.obj("id" -> -1))
      .cursor[Item](ReadPreference.primary)}

    val list: Future[List[Item]] = cursor.flatMap(_.collect[List]())

    val array: Future[JsArray] = list.map {
      item => Json.arr(item)
    }

    list.map{
      item => mongoHelpers.createNewList(item) match {
        case true => Redirect(routes.Application.index())
        case false => BadRequest("something happened")
      }
    }
  }

  //==========================DELETE======================================
  def deleteItem(id: Int) = Action {
    Item.items.remove(id)
    Redirect(routes.Application.index())
  }

  def deleteFromDB(id: Int)  =  Action.async {
    val itemId = () => if(!Item.items.isEmpty) Item.items(id).id else ""
    val itemRemove = itemCol.map{_.findAndRemove(Json.obj("id"->itemId()))}

    itemRemove.map {
      stuff => Ok("deleted succesffully")
    }
  }


  def formHandler(id: Int) = Action { implicit request: Request[AnyContent] =>
    val formResult = Item.itemForm.bindFromRequest
    formResult.fold({
      errors =>
        println(errors)
        BadRequest(views.html.index(errors, id))
    },{
      item => formHelper(item)
    })
  }

  def formHelper(item: Item): Result = {
    Item.items.filter(_.id == item.id) match{
      case x if x.length == 0 =>
        item.id = (Item.nId+1).toString
        itemCol.flatMap(_.insert(item))
        Item.items.append(item)
        Item.nId += 1
      case x if x.length == 1 =>
        x.head.replace(item)
    }
    println(Item.items)
    Redirect(routes.Application.index())
  }



}