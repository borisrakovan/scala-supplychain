package cz.cvut.fel.omo.foodchain.ecosystem

import cz.cvut.fel.omo.foodchain.blockchain._
import cz.cvut.fel.omo.foodchain.foodchain._
import cz.cvut.fel.omo.foodchain.foodchain.parties._
import cz.cvut.fel.omo.foodchain.foodchain.channels._

// TODO: test channels, all types of channel requests
class EcosystemSimulation {
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var currentTick: Int = 0
  val network: Network[FoodChainParty] = new Network

  private val foodMaterials1: List[FoodMaterial] =
    List[FoodMaterial](new Apple(12.04), new Pear(11.4), new Melon(10.2))
  private val foodMaterials2: List[FoodMaterial] =
    List[FoodMaterial](new Strawberry(10.26), new Strawberry(2.2))
  private val foodMaterials3: List[FoodMaterial] =
    List[FoodMaterial](new Banana(12.50), new Apple(9.99))

  private val channel1: PaymentChannel = new PaymentChannel()
  private val channel2: FoodChannel = new MeatChannel()

  private val allChannels: List[Channel] = List(channel1, channel2)

  private val party1 = new Farmer(network, allChannels, foodMaterials1, 1000)
  private val party2 = new Distributor(network, allChannels, foodMaterials2, 5000)
  private val party3 = new Regulator(network, allChannels, foodMaterials3, 10000)

  def simulationCycle(): Unit = {
    currentTick += 1
    println(s"--- TICK ${currentTick.toString()} ---")

    val allMessages = network.collectMessages()
    val channelMessages = allChannels.map(c => c.collectMessages())
    network.nodes.foreach { node =>
      val inbox = allMessages(node) ++ channelMessages.flatMap(ch => ch(node))
      node.act(inbox)
    }

    println(s"--------------\n")
  }

  def run(): Unit = {
    val parties = List(party1, party2, party3)
    val initialState = parties.flatMap { party =>
      party.foodMaterials.map(new Utxo[FoodMaterial](party, _)) :+
        new Utxo[Money](
          party,
          new Money(party.balance),
        )
    }

    List(party1, party2, party3).foreach { node =>
      node.initializeUtxos(initialState)
      network.registerNode(node)
      channel1.registerParty(node)
      channel2.registerParty(node)
    }
    println("Starting simulation")

    println(party1.getUtxos().map(_.toString()).mkString("\n"))
    // println(party2.getUtxos())

    0 until 10 foreach { _ =>
      simulationCycle()
    // Thread.sleep(1000)
    }
    println(party1.getUtxos().map(_.toString()).mkString("\n"))
    // println(party2.getUtxos())
  }
}
