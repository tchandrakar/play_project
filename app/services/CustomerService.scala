package services

import com.google.inject.ImplementedBy
import services.RequestDTOs.{AttributeRequestDTO, CustomerOnBoarding}
import services.ResponseDTOs.SuccessResponse
import services.impl.CustomerServiceImpl
import slick.dbio.DBIO

import scala.concurrent.Future

@ImplementedBy[CustomerServiceImpl]
trait CustomerService {
  def addCustomer(customer: CustomerOnBoarding): Future[(Long, Seq[Long])]

  def addCustomerBillInBulk(bills: Seq[Long], customerId: Long): Future[Seq[Long]]

  def updatePaymentStatus(billerBillId: Long, amountPaid: Long): Future[SuccessResponse]

  def fetchReceipts(customerIdentifiers: List[AttributeRequestDTO]): Future[SuccessResponse]
}
