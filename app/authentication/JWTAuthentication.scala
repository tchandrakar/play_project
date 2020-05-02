package authentication

import java.nio.charset.StandardCharsets
import java.util.Base64.getDecoder

import authentication.Authenticate.jti
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}

object JWTAuthentication {
  def getMillis = DateTime.now().getMillis / 1000

  case class AuthenticationFailed(reason: String) extends Exception(s"Authentication failed due to: $reason")

  lazy val schemeId = "4331-9bbc-6b93265b-fbe98dae-20b4"
  lazy val secretKey = "77521f510c0f-7e2f-40dc-a9d4-d05250325419"
  lazy val header = "{\"typ\":\"JWT\",\"alg\":\"HS256\"}"
  lazy val data = "{\"aud\":\"$schemeId\",\"jti\":\"$jti\",\"iat\":$getMillis}"
    .replace("$schemeId", schemeId)
    .replace("$jti", jti)
    .replace("$getMillis", getMillis.toString)
  lazy val dataLength = 4 * Math.ceil(data.length / 3)

  implicit class Authentication(json: JsValue) {

    implicit class ByteUtility(byteArray: Array[Byte]) {
      def convertToString = new String(byteArray, StandardCharsets.UTF_8)
    }

    implicit class StringUtility(strValue: String) {
      def fromBase64Url: String = {
        strValue.replaceAll("_", "/").replaceAll("-", "\\+")
          .concat("=" * (dataLength - strValue.length).toInt)
      }
    }

    def decode = {
      val signedToken = (json \ "Authorization").asOpt[String].getOrElse(throw AuthenticationFailed("Bearer token is not present")).split(" ").last
      val (token, _) = signedToken.splitAt(signedToken.lastIndexOf("."))
      val (_, encodedData) = token.splitAt(token.indexOf("."))
      val data = getDecoder.decode(getDecoder.decode(encodedData.substring(1).fromBase64Url)).convertToString
      val parseJson = Json.parse(data)
      (parseJson \ "aud").asOpt[String] match {
        case Some(value) => value
        case None => throw AuthenticationFailed("Authentication header has no scheme Id.")
      }
    }
  }

}

//import JWTAuthentication._
//val json = Json.obj("Authorization" ->
//"Bearer ZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKSVV6STFOaUo5.ZXlKaGRXUWlPaUkwTXpNeExUbGlZbU10Tm1JNU16STJOV0l0Wm1KbE9UaGtZV1V0TWpCaU5DSXNJbXAwYVNJNklqSXVPREUxTURRd01qTXdNRGd6TnpkRk1UUWlMQ0pwWVhRaU9qRTFPRGd6TVRrMU1UUjk.NUxuMkI1cmJLTzE3NHNoeUlIQ0tPS3N2WUFUNjc4SktvdEJJNUllc0lWST0")
//
//println(json.decode._2 \ "aud")
