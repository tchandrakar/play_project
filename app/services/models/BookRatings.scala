package services.models

import slick.lifted.{ForeignKeyQuery, ProvenShape, Tag}
import slick.jdbc.PostgresProfile.api._

case class BookRating(bookId: Long, rating1: Long, rating2: Long, rating3: Long, rating4: Long, rating5: Long)

class BookRatings (tag: Tag) extends Table[BookRating](tag, _tableName = "book_ratings") {

  def bookId = column[Long]("book_id", O.PrimaryKey)

  def rating1 = column[Long]("rating_1")
  def rating2 = column[Long]("rating_2")
  def rating3 = column[Long]("rating_3")
  def rating4 = column[Long]("rating_4")
  def rating5 = column[Long]("rating_5")

  def fKeyBookId: ForeignKeyQuery[Books, Book] = foreignKey(s"fkey_${tableName}_book_id_to_books", bookId, Books.query)(_.id)

  override def * : ProvenShape[BookRating] = (bookId, rating1, rating2, rating3,rating4, rating5) <> (BookRating.tupled, BookRating.unapply)
}

object BookRatings {
  lazy val query = TableQuery[BookRatings]
}