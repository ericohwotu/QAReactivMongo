package models

import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.collection.mutable.ListBuffer


case class Item(var id: String="NULL", var name: String, var price: BigDecimal, var description: String = "",
                var manufacturer: String = "", var warranty: Int,
                var discount: Option[BigDecimal] = Some(0), var seller: String = "", var image: String = ""){


  def replace(item: Item): Unit ={
    this.name = item.name
    this.price = item.price
    this.description = item.description
    this.manufacturer = item.manufacturer
    this.warranty = item.warranty
    this.discount = item.discount
    this.seller = item.seller
    this.image = item.image
  }

}

object Item {

  var nId = 2

  val itemForm: Form[Item] = Form(
    mapping(
      "id" -> text,
      "name" -> nonEmptyText,
      "price" -> bigDecimal(9,2),
      "description" -> text,
      "manufacturer" -> text,
      "warranty" -> default(number,720),
      "discount" -> optional(bigDecimal(9,2)),
      "seller" -> text,
      "image" -> text
    )(Item.apply _)(Item.unapply _))


  val items: ListBuffer[Item] = ListBuffer(
    Item(0.toString,"PS4", 245, "The ultimate in console gaming", "sony", 730),
    Item(1.toString,"XBOX ONE", 245, "The ultimate in console gaming", "Microsoft", 730),
    Item(2.toString,"Samsung Galaxy S8", 245, "The sexiest smartphone available", "Samsung", 730)
  )

  //mongodb stuff
  implicit val itemFormat = Json.format[Item]

//  //json readers
//  implicit val itemReader: Reads[Item] = (
//    (__ \ "id").read[String] and
//      (__ \ "name").read[String] and
//      (__ \ "price").read[BigDecimal] and
//      (__ \ "description").read[String] and
//      (__ \ "manufacturer").read[String] and
//      (__ \ "warranty").read[Int] and
//      (__ \ "discount").read[BigDecimal] and
//      (__ \ "seller").read[String] and
//      (__ \ "image").read[String]
//    )(Item.apply _)
//
//  //json writers
//  implicit val itemWriter: Writes[Item] = (
//    (__ \ "id").write[String] and
//      (__ \ "name").write[String] and
//      (__ \ "price").write[BigDecimal] and
//      (__ \ "description").write[String] and
//      (__ \ "manufacturer").write[String] and
//      (__ \ "warranty").write[Int] and
//      (__ \ "discount").write[BigDecimal] and
//      (__ \ "seller").write[String] and
//      (__ \ "image").write[String]
//    )(unlift(Item.unapply))

}

