package services.models

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

case class TransactionDetail(id: Long, billerBillId: Long, platformBillId: Long, platformTransactionRefID: Long,
                             uniquePaymentRefID: String, amountPaid: Long, billAmount: Long, transactionDate: String)


class TransactionDetails(tag: Tag) extends Table[TransactionDetail](tag, "transaction_details") {

  def id = column[Long]("id")
  def billerBillId = column[Long]("biller_bill_id")
  def platformBillId = column[Long]("platform_bill_id")
  def platformTransactionRefID = column[Long]("platform_transaction_ref_id")
  def uniquePaymentRefID = column[String]("unique_payment_ref_id")
  def amountPaid = column[Long]("amount_paid")
  def billAmount = column[Long]("bill_amount")
  def transactionDate = column[String]("transaction_date")

  def fkeybillerBillId= foreignKey(s"fkey_${tableName}_biller_bill_id_to_customer_bill_statuses",
    billerBillId, CustomerToBills.query )(_.billerBillID)

  override def * = (id, billerBillId, platformBillId, platformTransactionRefID,
    uniquePaymentRefID, amountPaid, billAmount, transactionDate) <> (TransactionDetail.tupled, TransactionDetail.unapply)
}

object TransactionDetails {
  lazy val query = TableQuery[TransactionDetails]
}