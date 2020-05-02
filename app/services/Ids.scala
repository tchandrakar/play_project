package services

import java.util.concurrent.ThreadLocalRandom

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

import scala.util.Random

object Ids {
  lazy val random: Random.type = scala.util.Random
  lazy val threadRandom: ThreadLocalRandom = ThreadLocalRandom.current()
  sealed trait WithString {
    def id: String

    def convertLongToId(longId: Long) = prefix + longId

    def fromString: Long

    def prefix: String

    def lowerBound: Long

    def upperBound: Long

    def generateRandomId: String = prefix + threadRandom.nextLong(lowerBound, upperBound)
  }

  sealed trait WithLong {
    def value: Long
  }


  case class PlatformBillId(id: String) extends WithString {
    override def fromString: Long = id.splitAt(4)._2.toLong

    override def prefix: String = "SETU"

    override def lowerBound: Long = 1e12.toLong

    override def upperBound: Long = 1e13.toLong
  }

  object PlatformBillId {
    implicit val formats: Format[PlatformBillId] = new Format[PlatformBillId] {
      override def writes(o: PlatformBillId): JsValue = Json.toJson(o.id)

      override def reads(json: JsValue): JsResult[PlatformBillId] = json.validate[String].map(PlatformBillId(_))
    }
  }

  case class PlatformTransactionRefID(id: String) extends WithString {
    override def fromString: Long = id.splitAt(3)._2.toLong

    override def prefix: String = "TXN"

    override def lowerBound: Long = 1e8.toLong

    override def upperBound: Long = 1e9.toLong
  }

  object PlatformTransactionRefID {
    implicit val formats: Format[PlatformTransactionRefID] = new Format[PlatformTransactionRefID] {
      override def writes(o: PlatformTransactionRefID): JsValue = Json.toJson(o.id)

      override def reads(json: JsValue): JsResult[PlatformTransactionRefID] = json.validate[String].map(PlatformTransactionRefID(_))
    }
  }

  case class ReceiptId(id: String) extends WithString {
    override def fromString: Long = id.splitAt(1)._2.toLong

    override def prefix: String = "R"

    override def lowerBound: Long = 1e11.toLong

    override def upperBound: Long = 1e12.toLong
  }

  case class Amount(value: Long) extends WithLong

  object Amount {
    implicit val formats: Format[Amount] = Json.format[Amount]
  }

  case class Bill(value: Long) extends WithLong

  object Bill {
    implicit val formats: Format[Bill] = Json.format[Bill]
  }

  case class UniquePaymentRefID(id: String) {

    def generateRandomUniquePaymentRefId = {
      random.alphanumeric(10).toString.toUpperCase + random.nextInt(900000) + 100000
    }

  }

  object UniquePaymentRefID {
    implicit val formats: Format[UniquePaymentRefID] = new Format[UniquePaymentRefID] {
      override def writes(o: UniquePaymentRefID): JsValue = Json.toJson(o.id)

      override def reads(json: JsValue): JsResult[UniquePaymentRefID] = json.validate[String].map(UniquePaymentRefID(_))
    }
  }

  object DateTimeFormatterObject {
    val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

    val jodaDateReads = Reads[DateTime](js =>
      js.validate[String].map[DateTime](dtString =>
        DateTime.parse(dtString, DateTimeFormat.forPattern(dateFormat))
      )
    )

    val jodaDateWrites: Writes[DateTime] = (d: DateTime) => JsString(d.toString())
  }
}
