package helpers

import models.Item

import play.api.mvc._


class MongoHelpers{

  def createNewList(items: List[Item]): Boolean = {
    Item.items.clear()
    Item.items ++= items
    true
  }
}
