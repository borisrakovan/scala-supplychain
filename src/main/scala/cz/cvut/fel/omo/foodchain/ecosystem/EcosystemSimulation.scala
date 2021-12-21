package cz.cvut.fel.omo.foodchain.ecosystem

// TODO: test channels, all types of channel requests
class EcosystemSimulation(val ecosystem: Ecosystem) {
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var currentTick: Int = 0

  // val reportManager = new ReportManager(ecosystem)

  def simulationCycle(): Unit = {
    currentTick += 1
    println(s"--- TICK ${currentTick.toString()} ---")

    val allMessages = ecosystem.network.collectMessages()
    val allChannelMessages = ecosystem.channels.map(c => c.collectMessages())

    ecosystem.parties.foreach { party =>
      val networkInbox = allMessages(party)
      val channelsInbox = allChannelMessages.flatMap(ch => ch(party))
      val inbox = (networkInbox ++ channelsInbox).sortBy(_.timestamp)
      party.act(inbox)
    }

    println(s"--------------\n")
  }

  def run(): Unit = {

    println("Starting simulation")

    // println(party1.getUtxos().map(_.toString()).mkString("\n"))

    0 until 20 foreach { _ =>
      // todo: communicate with user
      // if (!reportType not in reportManager.knownReportTypes) {
      //   // bust
      // }
      // reportManager.generateReport(reportManager.knownReportTypes(reportType))

      simulationCycle()
    }
    // println(party1.getUtxos().map(_.toString()).mkString("\n"))

  }
}
