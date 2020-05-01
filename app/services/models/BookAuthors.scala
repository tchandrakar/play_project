package services.models

import slick.lifted.{ForeignKeyQuery, ProvenShape, Tag}
import slick.jdbc.PostgresProfile.api._

case class BookAuthor(id: Long, authorId: Long, bookId: Long)

class BookAuthors(tag: Tag) extends Table[BookAuthor](tag, "book_authors") {

  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def authorId: Rep[Long] = column[Long]("author_id")
  def bookId: Rep[Long] = column[Long]("book_id")

  def fKeyAuthorId: ForeignKeyQuery[Authors, Author] = foreignKey(s"fkey_${tableName}_author_id_to_authors", authorId, Authors.query )(_.id)
  def fKeyBookId: ForeignKeyQuery[Books, Book] = foreignKey(s"fkey_${tableName}_book_id_to_books", bookId, Books.query )(_.id)

  def * : ProvenShape[BookAuthor] = (id, authorId, bookId) <> (BookAuthor.tupled, BookAuthor.unapply)
}

object BookAuthors {
  lazy val query = TableQuery[BookAuthors]
}