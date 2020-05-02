package authentication

import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, OFormat}
import play.api.mvc.{Request, Result, Results}
import authentication.JWTAuthentication._

import scala.concurrent.Future

object ControllerUtility {
  implicit class AuthenticateRequest(request: Request[JsValue]) {
    def performAuthentication(f: Future[Result]) = {
      val schemeIdInHeader = Json.toJson(request.headers.toSimpleMap).decode
      if(schemeIdInHeader == schemeId){
        f
      } else {
        Future.successful(Results.Unauthorized)
      }
    }

    def validateJson[T](f: T => Future[Result])(implicit formats: OFormat[T]) = {
      request.body.validate[T] match {
        case JsSuccess(value, _) => f(value)
        // TODO handle this badrequest
        case JsError(errors) => Future.successful(Results.BadRequest("Write something here"))
      }
    }
  }
}
