package controllers

import authentication.ControllerUtility._
import com.google.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.RequestDTOs.{FetchBillRequestDTO, FetchReceiptRequestDTO}

import scala.concurrent.Future

@Singleton
class ReceiptController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def fetchBills = Action.async(parse.json) { implicit request =>
    request.performAuthentication(request.validateJson[FetchBillRequestDTO] { fetchBillRequest =>
      Future.successful(Ok("Fetch the bill status here"))
    })
    // TODO handle this badrequest
  }


  def fetchReceipt = Action.async(parse.json) { implicit request =>
    request.performAuthentication(request.validateJson[FetchReceiptRequestDTO] { fetchReceiptRequest =>
      Future.successful(Ok("Fetch the bill status here"))
    })
    // TODO handle this badrequest
  }

}
