package cz.cvut.fel.omo.foodchain.ecosystem

case object EcosystemConfig {
  val Debug: Boolean = true
  val BlockSize: Int = 4
  val NumSimulationSteps: Int = 160
  val SimulationStepDuration: Int = 2 // seconds
  val OfferReportPeriod: Int = 5
  val AverageMineSuccessPeriod: Int = 2
  val HostileNode: Option[String] = Some("regulator") // or None, if the ecosystem is friendly
  val BlockMutationPeriodProbability: Double = 0.8
  val DoubleSpendPeriodProbability: Double = 0.8
}
