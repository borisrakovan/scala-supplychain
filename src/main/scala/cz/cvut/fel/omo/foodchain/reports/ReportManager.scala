package cz.cvut.fel.omo.foodchain.reports
import scala.util.{ Try, Success, Failure }

object ReportManager {
  val ReportGenerators: Map[String, ReportGenerator] = Map(
    "parties" -> PartiesReportGenerator,
    "materials" -> MaterialsReportGenerator,
    "security" -> SecurityReportGenerator,
    "transactions" -> TransactionsReportGenerator,
  )
}

class ReportManager {
  def generateReport(): Try[EcosystemReport] = Try {}
}

trait ReportGenerator {}
