package controllers

import authentication.ControllerUtility._
import com.google.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import services.CustomerService
import services.RequestDTOs.{FetchBillRequestDTO, FetchReceiptRequestDTO}
import services.impl.CustomerServiceImpl.InvalidReceiptFetchRequest
import utilities.DbUtils._

import scala.concurrent.Future
import scala.util.Try

@Singleton
class ReceiptController @Inject()(cc: ControllerComponents,
                                  customerService: CustomerService) extends AbstractController(cc) {

  def fetchBills = Action.async(parse.json) { implicit request =>
    request.performAuthentication(request.validateJson[FetchBillRequestDTO] { fetchBillRequest =>
      Try {
        customerService.fetchBills(fetchBillRequest.customerIdentifiers).safely
      }.flattenedEither.map {
        case Left(t: InvalidReceiptFetchRequest) => NotFound(failedResponse(NOT_FOUND, "customer-invalid", "Customer invalid", t.fieldValue))
        case Left(t: Throwable) => throw t
        case Right(result) => Ok(Json.toJson(result))
      }
    })
  }


  def fetchReceipt = Action.async(parse.json) { implicit request =>
    request.performAuthentication(request.validateJson[FetchReceiptRequestDTO] { fetchReceiptRequest =>
      Try {
        customerService.fetchReceipt(fetchReceiptRequest).safely
      }.flattenedEither.map {
        case Left(t: InvalidReceiptFetchRequest) =>
          NotFound(failedResponse(NOT_FOUND, "customer-invalid", "Customer invalid", t.fieldValue))
        case Left(t: Throwable) => throw t
        case Right(result) =>
          Ok(Json.obj("billerBillID" -> result.billerBillId, "platformBillID" -> s"SETU${result.platformBillId}",
          "platformTransactionRefID" -> s"TXN${result.platformTransactionRefID}",
            "receipt" -> Json.obj("id" -> s"R${result.id}", "date" -> result.transactionDate)))
      }
    })
  }

}
