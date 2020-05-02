package controllers

import com.google.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}
import authentication.ControllerUtility._
import play.api.libs.json.Json
import services.CustomerService
import services.RequestDTOs.{AddTransactionInBulk, CustomerOnBoarding}
import services.impl.CustomerServiceImpl.DataNotFound
import utilities.DbUtils._

import scala.util.Try
class CustomerController @Inject()(cc: ControllerComponents,
                                   customerService: CustomerService) extends AbstractController(cc) {
  def addCustomer = Action.async(parse.json) { implicit request =>
    request.performAuthentication(request.validateJson[CustomerOnBoarding]{ customerOnBoarding =>
      Try {
        customerService.addCustomer(customerOnBoarding).safely
      }.flattenedEither.map {
        case Left(t: DataNotFound) => throw t
        case Right(customerId) => Ok(Json.obj("customerId" -> customerId))
      }
    })
  }

  def addTransaction = Action.async(parse.json) { implicit request =>
    request.performAuthentication(request.validateJson[AddTransactionInBulk] { bulkTransaction =>
      Try {
        customerService.addCustomerBillInBulk(bulkTransaction.bills, bulkTransaction.customerId).safely
      }.flattenedEither.map {
        case Left(t: DataNotFound) => throw t
        case Right(billIds) => Ok(Json.obj("billIds" -> billIds))
      }
    })
  }
}