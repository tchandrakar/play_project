package authentication

import java.nio.charset.StandardCharsets
import java.util.Base64.getEncoder

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.joda.time.DateTime

object Authenticate {
  lazy val schemeId = "4331-9bbc-6b93265b-fbe98dae-20b4"
  lazy val secret = "77521f510c0f-7e2f-40dc-a9d4-d05250325419"

  def jti: String = Math.floor(Math.random() * 1e15) + ""

  implicit class ByteUtility(byteArray: Array[Byte]) {
    def convertToString = new String(byteArray, StandardCharsets.UTF_8)
  }

  implicit class StringUtility(source: String) {
    def encodeStringToBase64 = getEncoder.encode(source.getBytes()).convertToString

    def convertToBase64Url = {
      val bytesEncoded = getEncoder.encode(source.getBytes()).convertToString
      bytesEncoded.foldRight(("", true)) { case (char, (str, removed)) =>
        if (removed && char.equals('=')) (str, true)
        else {
          (char + str, false)
        }
      }._1.replaceAll("\\+", "-").replaceAll("/", "_")
    }

    def generateHMAC(sharedSecret: String) = {
      val secret = new SecretKeySpec(sharedSecret.getBytes, "HmacSHA256")
      val mac = Mac.getInstance("HmacSHA256")
      mac.init(secret)
      getEncoder().encodeToString(mac.doFinal(source.getBytes))
    }
  }

  def getMillis = DateTime.now().getMillis / 1000

  val header = "{\"typ\":\"JWT\",\"alg\":\"HS256\"}"
  val data = "{\"aud\":\"$schemeId\",\"jti\":\"$jti\",\"iat\":$getMillis}"
    .replace("$schemeId", schemeId)
    .replace("$jti", jti)
    .replace("$getMillis", getMillis.toString)

  val encodedHeader = header.encodeStringToBase64.convertToBase64Url
  val encodedData = data.encodeStringToBase64.convertToBase64Url

  val token = s"$encodedHeader.$encodedData"

  val signature = token.generateHMAC(secret).convertToBase64Url

  val signedBearerToken = s"Bearer $token.$signature"
}
