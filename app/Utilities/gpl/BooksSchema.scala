package Utilities.gpl

import Utilities.dtos.DTOs.{BookDTO, BookRatingDTO}
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.macros.derive._
import sangria.schema._
import services.impl.BooksServiceImpl

object BooksSchema {

  val books = Fetcher((ctx: BooksServiceImpl, _: Seq[Long]) => ctx.getBooks)(HasId(_.id))

  val RatingType = deriveObjectType[Unit, BookRatingDTO](
    ObjectTypeDescription("Book Rating")
  )

  val BookType = ObjectType(
    "Book",
    fields[Unit, BookDTO] (
      Field("id", LongType, resolve = _.value.id),
      Field("title", StringType, resolve = _.value.title),
      Field("authors", ListType(StringType), resolve = _.value.authors),
      Field("originalPublicationYear", IntType, resolve = _.value.originalPublicationYear),
      Field("isbn", LongType, resolve = _.value.isbn)
    )
  )

  val Id = Argument("id", LongType)

  val BooksQueryType = ObjectType("Query", fields[BooksServiceImpl, Unit] (
    Field("books", ListType(BookType),
      description = Some("Returns list of books"),
      resolve = c ⇒ c.ctx.getBooks)
  ))

  val schema = Schema(BooksQueryType)

}