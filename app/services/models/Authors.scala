package services.models

import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.PostgresProfile.api._

case class Author(id: Long, name: String)

class Authors(tag: Tag) extends Table[Author](tag, _tableName = "authors") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")

  override def * : ProvenShape[Author] = (id, name) <> (Author.tupled, Author.unapply)
}

object Authors {
  lazy val query = TableQuery[Authors]
}
