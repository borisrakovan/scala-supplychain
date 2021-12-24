package cz.cvut.fel.omo.foodchain.ecosystem

import cz.cvut.fel.omo.foodchain.reports.ReportManager
import cz.cvut.fel.omo.foodchain.ui.UserInterface
import cz.cvut.fel.omo.foodchain.common.Logger
import cz.cvut.fel.omo.foodchain.blockchain.BlockChain
import cz.cvut.fel.omo.foodchain.foodchain.{ HostileBehaviour, PartyBehaviour }

class EcosystemSimulation(val ecosystem: Ecosystem, val userInterface: UserInterface) {
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var currentTick: Int = 0

  private val reportManager: ReportManager = new ReportManager(ecosystem)
  private val hostileBehaviour: PartyBehaviour = new HostileBehaviour(
    EcosystemConfig.DoubleSpendPeriodProbability,
    EcosystemConfig.BlockMutationPeriodProbability,
  )
  def simulationCycle(): Unit = {
    currentTick += 1
    Logger.info(s"--- TICK ${currentTick.toString()} ---")

    val allMessages = ecosystem.network.collectMessages()
    val allChannelMessages = ecosystem.channels.map(c => c.collectMessages())

    ecosystem.parties.foreach { party =>
      val networkInbox = allMessages(party)
      val channelsInbox = allChannelMessages.flatMap(ch => ch(party))
      val inbox = (networkInbox ++ channelsInbox).sortBy(_.timestamp)
      party.act(inbox)
    }

    ecosystem.getHostileParty() match {
      case Some(party) => party.applyBehaviour(hostileBehaviour)
      case None =>
    }

    Logger.info(s"---------------\n")
  }

  def interactionCycle(): Unit = {
    val showReport = userInterface.getYesNoAnswer("Generate report?")
    if (showReport) {
      val reportType =
        userInterface.getUserSelection("Select a report type:", reportManager.getReportTypes())
      val report = reportManager.generateReport(reportType)
      userInterface.showReport(report)
      userInterface.requireConfirmation()
    }
  }

  def showSummaryReports(): Unit = {
    userInterface.showReport(reportManager.generateReport("transactions"))
    userInterface.showReport(reportManager.generateReport("parties"))
    userInterface.showReport(reportManager.generateReport("materials"))
    userInterface.showReport(reportManager.generateReport("security"))
  }

  def run(): Unit = {

    Logger.log("Starting simulation")

    0 until EcosystemConfig.NumSimulationSteps foreach { i =>
      simulationCycle()
    // if ((i + 1) % EcosystemConfig.OfferReportPeriod == 0)
    //   interactionCycle()
    // else
    // Thread.sleep(EcosystemConfig.SimulationStepDuration * 1000)
    }

    Logger.info("The simulation has ended")
    showSummaryReports()
  }
}
