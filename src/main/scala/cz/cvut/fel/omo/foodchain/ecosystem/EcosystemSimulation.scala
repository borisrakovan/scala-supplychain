package cz.cvut.fel.omo.foodchain.ecosystem

import cz.cvut.fel.omo.foodchain.reports.ReportManager
import cz.cvut.fel.omo.foodchain.UserInterface
import cz.cvut.fel.omo.foodchain.{ Logger, Config }
import cz.cvut.fel.omo.foodchain.foodchain.{ HostileBehaviour, PartyBehaviour }
import java.io.PrintWriter

class EcosystemSimulation(val ecosystem: Ecosystem, val userInterface: UserInterface) {
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var currentTick: Int = 0

  private val reportManager: ReportManager = new ReportManager(ecosystem)
  private val hostileBehaviour: PartyBehaviour = new HostileBehaviour(
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
    val reports = List(
      reportManager.generateReport("transactions"),
      reportManager.generateReport("parties"),
      reportManager.generateReport("materials"),
      reportManager.generateReport("security"),
    )
    reports.foreach(userInterface.showReport(_))

    Config.ReportFile match {
      case Some(filename) =>
        val content = reports.map(_.toString()).mkString("\n\n")
        val w = new PrintWriter(filename)
        w.write(content)
        w.close()

      case None =>
    }
  }

  def run(): Unit = {

    Logger.log("Starting simulation")

    0 until Config.NumSimulationSteps foreach { i =>
      simulationCycle()
      if ((i + 1) % Config.OfferReportPeriod == 0)
        interactionCycle()
      else
        Thread.sleep((Config.SimulationStepDuration * 1000).toLong)
    }

    for (p <- ecosystem.parties)
      p.blockChain.validate(p) match {
        case Some(violation) =>
          for (p2 <- ecosystem.parties)
            p2.securityLog.report(violation)
        case None =>
      }

    Logger.info("The simulation has ended")
    showSummaryReports()
  }
}
