package services

import org.joda.time.DateTime
import play.api.libs.json.{Format, JsResult, JsValue, Json, OFormat}
import services.Ids.Amount
import services.Ids.DateTimeFormatterObject._

object ResponseDTOs {

  case class InvalidStringStatus(str: String) extends Exception(s"$str is an invalid status.")

  sealed trait BillFetchStatus {
    def status: String
  }

  case object NoOutstanding extends BillFetchStatus {
    override def status: String = "NO_OUTSTANDING"
  }

  case object Available extends BillFetchStatus {
    override def status: String = "AVAILABLE"
  }

  object BillFetchStatus {
    val all = Set(NoOutstanding, Available)

    def fromString(str: String): BillFetchStatus = all.find(_.status == str).getOrElse(throw InvalidStringStatus(str))

    implicit val formats = new Format[BillFetchStatus] {
      override def writes(o: BillFetchStatus): JsValue = Json.toJson(o.status)

      override def reads(json: JsValue): JsResult[BillFetchStatus] = json.validate[String].map(fromString)
    }
  }

  sealed trait Recurrence {
    def rType: String
  }

  case object OneTime extends Recurrence {
    override def rType: String = "ONE_TIME"
  }

  case object MultipleTime extends Recurrence {
    override def rType: String = "MULTIPLE_TIME"
  }

  object Recurrence {
    val all = Set(OneTime, MultipleTime)

    def fromString(str: String): Recurrence = all.find(_.rType == str).getOrElse(throw InvalidStringStatus(str))

    implicit val formats = new Format[Recurrence] {
      override def writes(o: Recurrence): JsValue = Json.toJson(o.rType)

      override def reads(json: JsValue): JsResult[Recurrence] = json.validate[String].map(fromString)
    }
  }

  sealed trait AmountExactness {
    def exactness: String
  }

  case object Exact extends AmountExactness {
    override def exactness: String = "EXACT"
  }

  case object RoundOff extends AmountExactness {
    override def exactness: String = "ROUND_OFF"
  }

  object AmountExactness {
    val all = Set(Exact, RoundOff)

    def fromString(str: String): AmountExactness = all.find(_.exactness == str).getOrElse(throw InvalidStringStatus(str))

    implicit val formats = new Format[AmountExactness] {
      override def writes(o: AmountExactness): JsValue = Json.toJson(o.exactness)

      override def reads(json: JsValue): JsResult[AmountExactness] = json.validate[String].map(fromString)
    }
  }

  case class CustomerName(name: String)

  object CustomerName {
    implicit val formats: OFormat[CustomerName] = Json.format[CustomerName]
  }

  case class CustomerId(id: Long)

  object CustomerId {
    implicit val formats: OFormat[CustomerId] = Json.format[CustomerId]
  }

  case class AggregateTotal(displayName: String, amount: Amount)

  object AggregateTotal {
    implicit val formats: OFormat[AggregateTotal] = Json.format[AggregateTotal]
  }

  case class Aggregates(total: AggregateTotal)

  object Aggregates {
    implicit val formats: OFormat[Aggregates] = Json.format[Aggregates]
  }

  case class SingleBillDetail(billerBillID: Long, generatedOn: String, recurrence: Recurrence, amountExactness: AmountExactness,
                              customerAccount: CustomerId, aggregates: Aggregates)

  object SingleBillDetail {
    implicit val formats: OFormat[SingleBillDetail] = Json.format[SingleBillDetail]
  }

  case class BillDetails(billFetchStatus: BillFetchStatus, bills: Seq[SingleBillDetail])

  object BillDetails {
    implicit val formats: OFormat[BillDetails] = Json.format[BillDetails]
  }

  case class CustomerData(customer: CustomerName, billDetails: BillDetails)

  object CustomerData {
    implicit val formats: OFormat[CustomerData] = Json.format[CustomerData]
  }

  case class SuccessResponse(data: CustomerData)

  object SuccessResponse {
    implicit val formats: OFormat[SuccessResponse] = Json.format[SuccessResponse]
  }

}
