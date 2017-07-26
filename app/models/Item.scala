package models

import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json

import scala.collection.mutable.ListBuffer


case class Item(var id: String="NULL", var name: String, var price: BigDecimal, var description: String = "",
                var manufacturer: String = "", var warranty: Int,
                var discount: BigDecimal = 0, var seller: String = "", var image: String = ""){


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
      "discount" -> bigDecimal(9,2),
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

}

