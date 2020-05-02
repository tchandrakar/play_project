package services.models

import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.PostgresProfile.api._

case class Customer(id: Long, mobileNumber: Long, name: String, billFetchStatus: String, emailId: String)

class Customers(tag: Tag) extends Table[Customer](tag, "customers") {
  def customerId: Rep[Long] = column[Long]("customer_id", O.PrimaryKey)
  def mobileNumber: Rep[Long] = column[Long]("mobile_number")
  def name: Rep[String] = column[String]("name")
  def billFetchStatus: Rep[String] = column[String]("bill_fetch_status")
  def emailId: Rep[String] = column[String]("email_id")

  def * : ProvenShape[Customer] = (customerId, mobileNumber, name, billFetchStatus, emailId) <>
    (Customer.tupled, Customer.unapply)
}

object Customers {
  lazy val query = TableQuery[Customers]
}
