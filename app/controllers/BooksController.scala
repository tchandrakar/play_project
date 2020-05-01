package controllers

import Utilities.gpl.BooksSchema
import com.google.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}
import sangria.execution._
import sangria.execution.deferred.DeferredResolver
import sangria.parser.{QueryParser, SyntaxError}
import services.BooksService
import services.impl.BooksServiceImpl

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


@Singleton
class BooksController @Inject()(cc: ControllerComponents,
                                booksService: BooksService) extends AbstractController(cc) {

  implicit val ec = ExecutionContext.global

  def getAllBooks = Action.async(parse.json) { request ⇒
    val query = (request.body \ "query").as[String]

    val variables = (request.body \ "variables").toOption.flatMap {
      case JsString(vars) ⇒ Some(parseVariables(vars))
      case obj: JsObject ⇒ Some(obj)
      case _ ⇒ None
    }

    executeQuery(query, variables)
  }


  private def parseVariables(variables: String) =
    if (variables.trim == "" || variables.trim == "null") Json.obj() else Json.parse(variables).as[JsObject]

  private def executeQuery(query: String, variables: Option[JsObject]) =
    QueryParser.parse(query) match {
      case Success(queryAst) ⇒
        Executor.execute(BooksSchema.schema, queryAst, new BooksServiceImpl,
          deferredResolver = DeferredResolver.fetchers(BooksSchema.books)
        ).map(Ok(_))

      case Failure(error: SyntaxError) ⇒
        Future.successful(BadRequest(Json.obj("syntaxError" → error.getMessage)))

      case Failure(error) ⇒
        throw error
    }

}
