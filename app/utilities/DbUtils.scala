package utilities

import play.api.Logger
import services.models.{BillStatuses, CustomerBillStatuses, CustomerToBills, Customers, TransactionDetails}
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable
import slick.lifted.TableQuery

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}

object DbUtils {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  lazy val db = Database.forConfig("defaultDb")

  type TQuery = TableQuery[_ <: Table[_]]

  val tables: Map[String, TQuery] = Map(
    "customers" -> Customers.query,
    "customer_bill_statuses" -> CustomerBillStatuses.query,
    "customer_to_bills" -> CustomerToBills.query,
    "bill_status" -> BillStatuses.query,
    "transaction_details" -> TransactionDetails.query
  )

  def checkAndCreateTables(): Unit = {
    Logger.logger.info("Creating non existing tables.")

    def nonExistingTables: Future[Seq[TQuery]] = Future.sequence {
      tables.map { case (tableName, tableQuery) =>
        db.run(MTable.getTables(tableName).headOption.map(t => if (t.isEmpty) Some(tableQuery) else None))
      }.toSeq
    }.map { t: Seq[Option[TQuery]] => t.flatten }

    def create: Future[Any] = nonExistingTables.flatMap {
      case Nil => Future.successful(())
      case tQueries => db.run(DBIO.sequence(tQueries.map(_.schema).map(_.create)).transactionally)
    }

    Await.result(create, Duration.Inf)
    Logger.logger.info("Done creating tables.")
  }

  implicit class Safely[T](val f: Future[T]) {
    type SafelyReturned = Either[Throwable, T]

    /** Converts the Future into a 'recovered' Future that resolves to an Either block holding either the result or
     * a Throwable based on whether the Future could complete successfully.
     *
     * @return A Future with Right(result), if the Future completes successfully, or Left(t), where t is a Throwable,
     *         if the Future fails due to a thrown exception
     */
    def safely(implicit ec: ExecutionContext): Future[SafelyReturned] =
      f.map(Right(_): SafelyReturned).recover { case t => Left(t) }
  }

  implicit class TrySafely[R](val tryBlock: Try[Future[Either[Throwable, R]]]) {

    /**
     * Converts a Try-wrapped 'recovered' Future to an Either holding either the result or a Throwable arising from the
     * Future or from the Try block.
     */
    def flattenedEither: Future[Either[Throwable, R]] = tryBlock match {
      case Failure(t) => Future.successful(Left(t))
      case Success(res) => res
    }
  }
}