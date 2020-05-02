package services.models

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape, Tag}

case class BillStatus(id: Long, billerBillId: Long, amount: Long, updatedOn: String, superSeededBy: Option[Long])

class BillStatuses(tag: Tag) extends Table[BillStatus](tag, "bill_status") {
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def billerBillID: Rep[Long] = column[Long]("biller_bill_id")
  def amount: Rep[Long] = column[Long]("amount")
  def updatedOn: Rep[String] = column[String]("updated_on")
  def superSeededBy: Rep[Option[Long]] = column[Option[Long]]("superseded_by")

  def fkeybillerBillId: ForeignKeyQuery[CustomerToBills, CustomerToBill] = foreignKey(s"fkey_${tableName}_biller_bill_id_to_customer_to_bills",
    billerBillID, CustomerToBills.query )(_.billerBillID)
  override def * : ProvenShape[BillStatus] = (id, billerBillID, amount, updatedOn, superSeededBy) <>
    (BillStatus.tupled, BillStatus.unapply)
}

object BillStatuses {
  lazy val query = TableQuery[BillStatuses]
}