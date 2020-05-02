package services.models

import services.ResponseDTOs.InvalidStringStatus
import slick.lifted.{ForeignKeyQuery, ProvenShape, Tag}
import slick.jdbc.PostgresProfile.api._

case class CustomerBillStatus(id: Long, customerId: Long, updateReason: String, updatedOn: String, superSededBy: Option[Long])

case class CustomerBillStatuses(tag: Tag) extends Table[CustomerBillStatus](tag, "customer_bill_statuses") {
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def customerId: Rep[Long] = column[Long]("customer_id")
  def updateReason: Rep[String] = column[String]("update_reason")
  def updatedOn: Rep[String] = column[String]("updated_on")
  def superSeededBy: Rep[Option[Long]] = column[Option[Long]]("superseded_by")

  def fkeyCustomerId: ForeignKeyQuery[Customers, Customer] = foreignKey(s"fkey_${tableName}_customer_id_to_customers",
    customerId, Customers.query )(_.customerId)
  override def * : ProvenShape[CustomerBillStatus] = (id, customerId, updateReason, updatedOn, superSeededBy) <>
    (CustomerBillStatus.tupled, CustomerBillStatus.unapply)
}

object CustomerBillStatuses {
  lazy val query = TableQuery[CustomerBillStatuses]
}