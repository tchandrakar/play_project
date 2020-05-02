package services.models

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

case class CustomerToBill(billerBillID: Long, customerId: Long, generatedOn: String, recurrence: String,
                          amountExactness: String, amount: Long)


class CustomerToBills(tag: Tag) extends Table[CustomerToBill](tag, "customer_to_bills") {
  def billerBillID: Rep[Long] = column[Long]("biller_bill_id")
  def customerId: Rep[Long] = column[Long]("customer_id")
  def generatedOn: Rep[String] = column[String]("generated_on")
  def recurrence: Rep[String] = column[String]("recurrence")
  def amountExactness: Rep[String] = column[String]("amount_exactness")
  def amount: Rep[Long] = column[Long]("amount")

  override def * : ProvenShape[CustomerToBill] = (billerBillID, customerId, generatedOn, recurrence, amountExactness, amount) <>
    (CustomerToBill.tupled, CustomerToBill.unapply)
}

object CustomerToBills {
  lazy val query = TableQuery[CustomerToBills]
}