package Utilities.dtos

object DTOs {

  case class BookRatingDTO(averageRating: Long, ratingCount: Long)
  case class BookDTO(id: Long, title: String, authors: List[String], originalPublicationYear: Int, isbn: Long)
}
