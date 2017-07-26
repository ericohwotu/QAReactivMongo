package controllers

import javax.inject.Inject

import models.Item
import play.api.i18n.{I18nSupport, MessagesApi}

import scala.concurrent.Future
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json.collection._
import reactivemongo.play.json._
import reactivemongo.play.json.collection.{JSONCollection, JsCursor}
import JsCursor._
import play.api.libs.json._
import reactivemongo.api._

class Application @Inject() (val messagesApi: MessagesApi, val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents with I18nSupport {

  def itemCol: Future[JSONCollection] = database.map(_.collection[JSONCollection]("persons"))

  def add = Action.async {
    val result = itemCol.flatMap(_.insert(Item.items.head))
    result.map(_ => Ok("worked"))
  }

  def find(name: String) = Action.async {
    val cursor: Future[Cursor[JsObject]] = itemCol.map{_.find(Json.obj("name"->name)).
      cursor[JsObject](ReadPreference.primary)}

    val list: Future[List[JsObject]] = cursor.flatMap(_.collect[List]())

    val array: Future[JsArray] = list.map {
      item => Json.arr(item)
    }

    array.map{
      item => Ok(item)
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

  def index(id: Int=0) = Action {
        Ok (views.html.index(Item.itemForm, id))
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