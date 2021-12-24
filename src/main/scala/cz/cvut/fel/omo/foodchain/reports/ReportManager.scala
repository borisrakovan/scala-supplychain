package cz.cvut.fel.omo.foodchain.reports
import cz.cvut.fel.omo.foodchain.ecosystem.Ecosystem

object ReportManager {
  val ReportGenerators: Map[String, ReportGenerator] = Map(
    "parties" -> new PartiesReportGenerator(),
    "materials" -> new MaterialsReportGenerator(),
    "security" -> new SecurityReportGenerator(),
    "transactions" -> new TransactionsReportGenerator(),
  )
}

class ReportManager(val ecosystem: Ecosystem) {
  def getReportTypes(): List[String] = ReportManager.ReportGenerators.keySet.toList

  def generateReport(reportType: String): Report = {
    val gen = ReportManager.ReportGenerators(reportType)
    gen.generate(ecosystem)
  }
}
