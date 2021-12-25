package cz.cvut.fel.omo.foodchain

case object Config {
  val Debug: Boolean = false
  // Number of transactions in one block
  val BlockSize: Int = 4
  // Number of discrete steps that the simulation should perform
  val NumSimulationSteps: Int = 90
  // Duration of one simulation step in seconds
  val SimulationStepDuration: Double = 0.8
  // Number of steps after which the user should be given a possibility to generate a report
  val OfferReportPeriod: Int = 20
  // The speed with which the nodes mine blocks
  val AverageMineSuccessPeriod: Int = 2
  // Number of leading zeros that have to be guessed in the POW protocol
  val ProofOfWorkDifficulty: Int = 2
  // Title of the node that intentionally performs violations in order for the program to ilustrate their correct detection
  // Use None if the ecosystem should be friendly
  val HostileNode: Option[String] = Some("regulator")
  // Probability that the hostile node will try to mutate its blockchain
  val BlockMutationProbability: Double = 0.8
  // Probability that the hostile node will try to doublespend some of its UTXOs in one simulation period
  val DoubleSpendPeriodProbability: Double = 0.03
  // The name of the file to which the final reports should be generated
  // or None if no reports should be persisted
  val ReportFile: Option[String] = Some("summaryReports.txt")
}
