package services

import play.api.libs.json.{Format, Json, OFormat}
import services.Ids.{Amount, Bill, PlatformBillId, PlatformTransactionRefID, UniquePaymentRefID}

object RequestDTOs {

  case class AttributeRequestDTO(attributeName: String, attributeValue: String)

  object AttributeRequestDTO {
    implicit val formats: OFormat[AttributeRequestDTO] = Json.format[AttributeRequestDTO]
  }

  case class FetchBillRequestDTO(customerIdentifiers: List[AttributeRequestDTO])

  object FetchBillRequestDTO {
    implicit val formats: OFormat[FetchBillRequestDTO] = Json.format[FetchBillRequestDTO]
  }


  case class PaymentDetails(platformTransactionRefID: PlatformTransactionRefID, uniquePaymentRefID: UniquePaymentRefID,
                            amountPaid: Amount, billAmount: Bill)

  object PaymentDetails {
    implicit val formats: Format[PaymentDetails] = Json.format[PaymentDetails]
  }

  case class FetchReceiptRequestDTO(billerBillID: Long, platformBillID: PlatformBillId, paymentDetails: PaymentDetails)

  object FetchReceiptRequestDTO {
    implicit val formats: OFormat[FetchReceiptRequestDTO] = Json.format[FetchReceiptRequestDTO]
  }

  case class CustomerOnBoarding(name: String, mobileNumber: Long, emailId: String)

  object CustomerOnBoarding {
    implicit val formats: OFormat[CustomerOnBoarding] = Json.format[CustomerOnBoarding]
  }

  case class AddTransactionInBulk(customerId: Long, bills: Seq[Long])

  object AddTransactionInBulk {
    implicit val formats: OFormat[AddTransactionInBulk] = Json.format[AddTransactionInBulk]
  }
}
