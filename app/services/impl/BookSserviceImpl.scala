package services.impl

import Utilities.db.DbUtils
import Utilities.dtos.DTOs.BookDTO
import com.google.inject.Singleton
import services.BooksService
import services.models._
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class BooksServiceImpl {

  implicit val ec = ExecutionContext.global

  private val booksQuery = Books.query
  private val authorsQuery = Authors.query
  private val bookAuthors = BookAuthors.query
  private val bookRatingsQuery = BookRatings.query

   def getBooks: Future[List[BookDTO]] = {
    DbUtils.db.run(getBooksDBIO)
  }

   def getBookById(id: Long): Future[Option[BookDTO]] = Future.successful(None)

  private def getBooksDBIO: DBIO[List[BookDTO]] = {

   val query = for {
      books <- booksQuery
      bookAuthors <- bookAuthors if books.id === bookAuthors.bookId
      authors <- authorsQuery if authors.id === bookAuthors.authorId
      bookRatings <- bookRatingsQuery if bookRatings.bookId === books.id
    } yield (books, authors)

    query.result.map(_.groupBy(_._1.id).map { case (_, r) =>
      /*val br = r.headOption.fold(throw new Exception("Ratings not found")) (_._3)
      val ratingsCount = br.rating1 + br.rating2 + br.rating3 + br.rating4 + br.rating5
      val averageRatings = if(ratingsCount > 0) {
        val weightedRatings = br.rating1 + br.rating2 * 2 + br.rating3 * 3 + br.rating4 * 4 + br.rating5 * 5
        weightedRatings / ratingsCount
      } else 0

      val ratingDTO = BookRatingDTO(averageRatings, ratingsCount)*/

      val authors = r.map(_._2).map(_.name).toList
      if(authors.isEmpty) throw new Exception("Empty authors")

      val b = r.headOption.fold(throw new Exception("Book not found")) (_._1)

      BookDTO(b.id, b.title, authors, b.originalPublicationYear, b.isbn)
    }(collection.breakOut))
  }
}
