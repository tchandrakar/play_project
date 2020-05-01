package services

import Utilities.dtos.DTOs.BookDTO

import scala.concurrent.Future

trait BooksService {

  def getBooks: Future[List[BookDTO]]

  def getBookById(id: Long): Future[Option[BookDTO]]

}
