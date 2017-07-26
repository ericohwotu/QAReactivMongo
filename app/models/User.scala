package models

import play.api.libs.json.Json

case class User(name: String, age: Int)

object JsonFormats {
  implicit val userFormat = Json.format[User]
}
