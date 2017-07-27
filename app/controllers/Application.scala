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
import play.api.data.Form
import play.api.libs.json._
import reactivemongo.api._

import scala.collection.mutable.ListBuffer


class Application @Inject()
(val messagesApi: MessagesApi, val reactiveMongoApi: ReactiveMongoApi, val navbarHelpers: NavbarHelpers,
 val mongoHelpers: MongoHelpers) extends Controller
  with MongoController with ReactiveMongoComponents with I18nSupport {

  // pages
  val home = (id: Int, itemForm: Form[Item]) => Ok (views.html.home(navbarHelpers.homePage, Item.items, id)(itemForm))
  val search = (id: Int, list: ListBuffer[Item]) => Ok (views.html.search(navbarHelpers.searchPage, list, id)(Item.searchForm))


  //the collection for the items
  def itemCol: Future[JSONCollection] = database.map(_.collection[JSONCollection]("ItemsCollection"))


  def index(id: Int=0) = Action {
    Ok (views.html.home(navbarHelpers.homePage, Item.items, id)(Item.itemForm))
  }


  //===============================ADD====================================/
  def add = Action.async {
    val result = itemCol.flatMap(_.insert(Item.items.head))
    result.map(_ => Ok("worked"))
  }

  //===============================GET====================================/
  def find(name: String) = Action.async {
    val cursor: Future[Cursor[Item]] = itemCol.map{_.find(Json.obj("name"->name)).
      cursor[Item](ReadPreference.primary)}

    val list: Future[List[Item]] = cursor.flatMap(_.collect[List]())

    list.map{
      item =>
        Ok(item.toString())
    }
  }

  def getAll(id: Int, isNew: Option[String]) = Action.async {
    val cursor: Future[Cursor[Item]] = itemCol.map{_.find(Json.obj())
      .sort(Json.obj("id" -> -1))
      .cursor[Item](ReadPreference.primary)}

    val list: Future[List[Item]] = cursor.flatMap(_.collect[List]())

    list.map{
      items => mongoHelpers.createNewList(items) match {
        case true =>

          Item.setGlobalID(items)

          isNew match {

            case None => home(id, Item.itemForm.fill(items(id)))

            case _ => home(id, Item.itemForm)

          }
        case false =>

          BadRequest("something happened")
      }
    }
  }


  //==========================DELETE======================================//
  def deleteItem(id: Int) = Action {

    Item.items.remove(id)

    Redirect(routes.Application.index())
  }

  def deleteFromDB(id: Int)  =  Action.async {

    val itemId = () => if(!Item.items.isEmpty) Item.items(id).id else ""

    val itemRemove = itemCol.map{_.findAndRemove(Json.obj("id"->itemId()))}

    itemRemove.map {
      stuff => Redirect(routes.Application.getAll(isNew = None))
    }
  }


  //==========================FORM========================================//
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
        jsonUpdatHelper(item)
        x.head.replace(item)
    }
    println(Item.items)
    Redirect(routes.Application.getAll(isNew = None))
  }

  def jsonUpdatHelper(item: Item) ={
    val modifier = Json.obj("$set" -> Json.obj("name"->item.name,
      "price"->item.price, "description" -> item.description,
      "manufacturer" -> item.manufacturer, "warranty" -> item.warranty,
      "discount" -> item.discount, "seller" -> item.seller, "image" -> item.image))

    itemCol.map{
      _.findAndUpdate(Json.obj("id"->item.id),modifier)
    }
  }

  //========================================SEARCH===========================/
  def searchPage = Action{
    search(0, ListBuffer())
  }

  def searchFormHandler(id: Int) = Action { implicit request: Request[AnyContent] =>
    println("="*50)
    val searchResult = Item.searchForm.bindFromRequest


    searchResult.fold({
      errors =>
        println("="*70)
        println(getString(searchResult.data)(" "))
        println("="*70)

        BadRequest(views.html.index(errors, id))
    },{
      item => formHelper(item)
    })
  }

  def getString(m: Map[String,String])(res: String): Map[String,String] = m.filter(_._2 != "").filter(_._1 != "id")

}