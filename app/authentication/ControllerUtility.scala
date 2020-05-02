package authentication

import authentication.JWTAuthentication._
import play.api.http.Status
import play.api.libs.json._
import play.api.mvc.{Request, Result, Results}

import scala.concurrent.Future

object ControllerUtility {
  implicit class AuthenticateRequest(request: Request[JsValue]) {
    def performAuthentication(f: Future[Result]) = {
      val schemeIdInHeader = Json.toJson(request.headers.toSimpleMap).decode
      if(schemeIdInHeader == schemeId){
        f
      } else {
        Future.successful(Results.Unauthorized(failedResponse(Status.UNAUTHORIZED,
          "invalid-scheme-id", "Invalid Scheme Id", "Scheme id in the headers are invalid")))
      }
    }

    def validateJson[T](f: T => Future[Result])(implicit formats: OFormat[T]) = {
      request.body.validate[T] match {
        case JsSuccess(value, _) => f(value)
        case JsError(errors) => Future.successful(Results.BadRequest(failedResponse(Status.BAD_REQUEST,
          "json-is-invalid", "Json is invalid", s"$errors")))
      }
    }
  }

  def failedResponse(result: Int, code: String, title: String, description: String) = {
    Json.obj("status" -> result, "success" -> false,
      "error" -> Json.obj("code" -> code, "title" -> title, "traceId" -> "", "description" -> description,
        "param" -> "", "docURL" -> ""))
  }
}
