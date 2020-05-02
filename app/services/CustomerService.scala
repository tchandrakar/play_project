package services

import com.google.inject.ImplementedBy
import services.RequestDTOs.{AttributeRequestDTO, CustomerOnBoarding, FetchReceiptRequestDTO}
import services.ResponseDTOs.SuccessResponse
import services.impl.CustomerServiceImpl
import services.models.TransactionDetail
import slick.dbio.DBIO

import scala.concurrent.Future

@ImplementedBy[CustomerServiceImpl]
trait CustomerService {
  def addCustomer(customer: CustomerOnBoarding): Future[Long]

  def addCustomerBillInBulk(bills: Seq[Long], customerId: Long): Future[Seq[Long]]

  def updatePaymentStatus(billerBillId: Long, amountPaid: Long, transactionDetail: TransactionDetail): Future[SuccessResponse]

  def fetchBills(customerIdentifiers: List[AttributeRequestDTO]): Future[SuccessResponse]

  def fetchReceipt(fetchReceiptRequestDTO: FetchReceiptRequestDTO): Future[TransactionDetail]
}
