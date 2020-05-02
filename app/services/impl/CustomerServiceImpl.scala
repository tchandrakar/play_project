package services.impl

import java.util.concurrent.ThreadLocalRandom

import com.google.inject.Singleton
import org.joda.time.DateTime
import services.{CustomerService, RequestDTOs}
import services.Ids.{Amount, DateTimeFormatterObject, PlatformBillId, PlatformTransactionRefID, ReceiptId, UniquePaymentRefID}
import services.RequestDTOs.{AttributeRequestDTO, CustomerOnBoarding, FetchReceiptRequestDTO}
import services.ResponseDTOs._
import services.impl.CustomerServiceImpl.{DataNotFound, InvalidReceiptFetchRequest, OnlyOneTimePaymentAllowedException}
import services.models._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

@Singleton
class CustomerServiceImpl extends CustomerService {
  lazy val db = Database.forConfig("defaultDb")
  lazy val customerQuery = Customers.query
  lazy val customerBillStatusQuery = CustomerBillStatuses.query
  lazy val customerToBillsQuery = CustomerToBills.query
  lazy val billStatusesQuery = BillStatuses.query
  lazy val transactionDetailsQuery = TransactionDetails.query
  lazy val (mKey, nameKey, customerIdKey, emailIdKey) = ("mobileNumber", "name", "customerId", "emailId")

  def now: String = DateTime.now().toString(DateTimeFormatterObject.dateFormat)

  implicit class IdUtility(ids: Seq[Long]) {
    val idSet: Set[Long] = ids.toSet

    val lCBound: Long = 1e10.toLong
    val rCBound: Long = 1e11.toLong

    def getRandomCustomerId: Long = {
      val id = ThreadLocalRandom.current().nextLong(lCBound, rCBound)
      if (idSet.contains(id)) {
        getRandomCustomerId
      } else {
        id
      }
    }

    val lBBound: Long = 1e11.toLong
    val rBBound: Long = 1e12.toLong

    def getRandomBillId: Long = {
      val id = ThreadLocalRandom.current().nextLong(lBBound, rBBound)
      if (idSet.contains(id)) {
        getRandomCustomerId
      } else {
        id
      }
    }
  }

  override def addCustomer(customerOnBoarding: CustomerOnBoarding): Future[Long] = db.run {
    (for {
      id <- customerQuery.map(_.customerId).result.map(_.getRandomCustomerId)
      _ <- updateCustomerBillStatus(id, NoOutstanding)
      _ <- customerQuery += Customer(id, customerOnBoarding.mobileNumber,
        customerOnBoarding.name, NoOutstanding.status, customerOnBoarding.emailId)

    } yield id).transactionally
  }

  override def addCustomerBillInBulk(bills: Seq[Long], customerId: Long): Future[Seq[Long]] = {
    db.run(addCustomerBillInBulkDBIO(bills, customerId))
  }

  override def updatePaymentStatus(billerBillId: Long, amountPaid: Long, transactionDetail: TransactionDetail): Future[SuccessResponse] = db.run {
    for {
      customerToBill <- customerToBillsQuery.filter(_.billerBillID === billerBillId).result.headOption
        .map(_.getOrElse(throw DataNotFound(billerBillId)))

      customer <- customerQuery.filter(_.customerId === customerToBill.customerId)
        .result.headOption.map(_.getOrElse(throw DataNotFound(customerToBill.customerId)))

      (recurrence, amountExactness, newAmount) <- {
        val recurrence = Recurrence.fromString(customerToBill.recurrence)
        val amountExactness = AmountExactness.fromString(customerToBill.amountExactness)
        if (recurrence == OneTime && (amountPaid - customerToBill.amount<0)) {
          throw OnlyOneTimePaymentAllowedException(billerBillId)
        } else {
          val newAmount = Math.max(amountPaid - customerToBill.amount, 0)
          customerToBillsQuery.filter(_.billerBillID === customerToBill.billerBillID).map(_.amount)
            .update(newAmount).map(_ => (recurrence, amountExactness, newAmount))
        }
      }
      customerToBills <- customerToBillsQuery.filter(_.customerId === customer.id).result.map(_.filter(_.amount>0))
      _ <- if (customerToBills.isEmpty){
        customerQuery.filter(_.customerId === customer.id).map(_.billFetchStatus).update(NoOutstanding.status)
      } else DBIO.successful(())
      _ <- updateBillStatus(billerBillId, newAmount)
      _ <- transactionDetailsQuery += transactionDetail
    } yield {
      val (billFetchStatus, displayName) = if(newAmount==0) (NoOutstanding, "No outstanding") else (Available, "Total outstanding")
      SuccessResponse(CustomerData(CustomerName(customer.name), BillDetails(billFetchStatus, List(SingleBillDetail(billerBillId, customerToBill.generatedOn, recurrence, amountExactness, CustomerId(customerToBill.customerId),
        Aggregates(AggregateTotal(displayName, Amount(newAmount))))))))
    }
  }

  override def fetchBills(customerIdentifiers: List[AttributeRequestDTO]): Future[SuccessResponse] = db.run {
    def isAllDigits(x: String) = x forall Character.isDigit
    val mNo = customerIdentifiers.filter(_.attributeName == mKey)
    val names = customerIdentifiers.filter(_.attributeName == nameKey)
    val customerIds = customerIdentifiers.filter(_.attributeName == customerIdKey)
    val emailIds = customerIdentifiers.filter(_.attributeName == emailIdKey)
    if(mNo.length>1 || names.length>1 || customerIds.length>1 || emailIds.length>1) throw InvalidReceiptFetchRequest(customerIdentifiers.toString)
    val (mobile, name, customerId, emailId) = (mNo.headOption, names.headOption, customerIds.headOption, emailIds.headOption)
    for {
      customersWithMobileFilter <- mobile match {
        case Some(value) if isAllDigits(value.attributeValue) => customerQuery.filter(_.mobileNumber === value.attributeValue.toLong).result
        case None => customerQuery.result
        case _ => throw InvalidReceiptFetchRequest(mKey)
      }
      customersWithNameFilter = name match {
        case Some(value) => customersWithMobileFilter.filter(_.name == value.attributeValue)
        case None => customersWithMobileFilter
      }
      customersWithCustomerIdFilter = customerId match {
        case Some(value) if isAllDigits(value.attributeValue) => customersWithNameFilter.filter(_.id == value.attributeValue.toLong)
        case None => customersWithNameFilter
        case _ => throw InvalidReceiptFetchRequest(customerIdKey)
      }
      customers = emailId match {
        case Some(value) => customersWithCustomerIdFilter.filter(_.name == value.attributeValue)
        case None => customersWithCustomerIdFilter
      }
      customer = if(customers.length>1){
        throw InvalidReceiptFetchRequest(s"Multiple customer found with the filters.")
      } else if(customers.isEmpty){
        throw InvalidReceiptFetchRequest(s"Invalid request.")
      } else {
        customers.head
      }
      allBills <- customerToBillsQuery.filter(_.customerId === customer.id).result
    } yield {
      val allBillDetails = allBills.map { bill =>
        val displayName = "Total outstanding"
        SingleBillDetail(bill.billerBillID, bill.generatedOn, Recurrence.fromString(bill.recurrence),
          AmountExactness.fromString(bill.amountExactness), CustomerId(customer.id), Aggregates(AggregateTotal(displayName, Amount(bill.amount))))
      }
      SuccessResponse(CustomerData(CustomerName(customer.name), BillDetails(BillFetchStatus.fromString(customer.billFetchStatus), allBillDetails)))
    }
  }

  protected[services] def addCustomerBillInBulkDBIO(bills: Seq[Long], customerId: Long): DBIO[Seq[Long]] = {
    (for {
      allIds <- customerToBillsQuery.map(_.billerBillID).result
      ids <- DBIO.sequence(bills.map { billAmount =>
        if (billAmount != 0) {
          val id = allIds.getRandomBillId
          val platformBillId = PlatformBillId(PlatformBillId("").generateRandomId).fromString
          val platformTransactionrefId = PlatformTransactionRefID(PlatformTransactionRefID("").generateRandomId).fromString
          val rId = ReceiptId(ReceiptId("").generateRandomId).fromString
          for {
            _ <- customerToBillsQuery += CustomerToBill(id, customerId, now, Random.shuffle(Recurrence.all).head.rType,
              Random.shuffle(AmountExactness.all).head.exactness, billAmount)
            _ <- transactionDetailsQuery += TransactionDetail(rId, id, platformBillId, platformTransactionrefId
              , UniquePaymentRefID("").generateRandomUniquePaymentRefId, 0, billAmount, now)
            _ <- updateBillStatus(id, billAmount)
          } yield Some(id)
        }
        else {
          DBIO.successful(None)
        }
      })
    } yield ids.flatten).transactionally
  }

  protected[services] def updateCustomerBillStatus(id: Long, updateReason: BillFetchStatus): DBIO[Unit] = {
    (for {
      customer <- customerQuery.filter(_.customerId === id).result.headOption.map(_.getOrElse(throw DataNotFound(id)))
      lastStatus <- customerBillStatusQuery.filter(r => r.customerId === customer.id && r.superSeededBy.isEmpty).result.headOption
      _ <- customerBillStatusQuery += CustomerBillStatus(-1, customer.id, updateReason.status, now, lastStatus.map(_.id))
    } yield ()).transactionally
  }

  protected[services] def updateBillStatus(billerBillId: Long, billAmount: Long): DBIO[Unit] = {
    (for {
      customerToBill <- customerToBillsQuery.filter(_.billerBillID === billerBillId)
        .result.headOption.map(_.getOrElse(throw DataNotFound(billerBillId)))
      lastBillStatus <- billStatusesQuery.filter(r => r.billerBillID === billerBillId && r.superSeededBy.isEmpty).result.headOption
      _ <- billStatusesQuery += BillStatus(-1, customerToBill.billerBillID, billAmount, now, lastBillStatus.map(_.id))
    } yield ()).transactionally
  }

  override def fetchReceipt(fetchReceiptRequestDTO: FetchReceiptRequestDTO): Future[TransactionDetail] = {
    val rId = ReceiptId(ReceiptId("").generateRandomId).fromString
    val date = now
    val transactionDetail = TransactionDetail(rId, fetchReceiptRequestDTO.billerBillID, fetchReceiptRequestDTO.platformBillID.fromString,
      fetchReceiptRequestDTO.paymentDetails.platformTransactionRefID.fromString, fetchReceiptRequestDTO.paymentDetails.uniquePaymentRefID.id,
      fetchReceiptRequestDTO.paymentDetails.amountPaid.value, fetchReceiptRequestDTO.paymentDetails.billAmount.value, date)
    updatePaymentStatus(fetchReceiptRequestDTO.billerBillID, fetchReceiptRequestDTO.paymentDetails.amountPaid.value, transactionDetail)
      .map(_ => transactionDetail)
  }
}

object CustomerServiceImpl {
  case class DataNotFound(id: Long) extends Exception(s"$id is an invalid id.")
  case class OnlyOneTimePaymentAllowedException(id: Long) extends Exception(s"$id has to be paid full in one go.")
  case class InvalidReceiptFetchRequest(fieldValue: String) extends Exception(s"$fieldValue can only be one value.")
}
