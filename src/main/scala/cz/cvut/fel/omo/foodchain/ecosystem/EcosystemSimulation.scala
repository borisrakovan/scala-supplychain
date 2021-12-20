package cz.cvut.fel.omo.foodchain.ecosystem

import cz.cvut.fel.omo.foodchain.blockchain._
import cz.cvut.fel.omo.foodchain.foodchain._
import cz.cvut.fel.omo.foodchain.foodchain.parties._
import cz.cvut.fel.omo.foodchain.foodchain.channels._

// TODO: test channels, all types of channel requests
class EcosystemSimulation {
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var currentTick: Int = 0
  val network: NetworkImpl = new NetworkImpl

  private val foodMaterials1: List[FoodMaterial] =
    List[FoodMaterial](
      new FoodMaterial("Apple", 12.04),
      new FoodMaterial("Pear", 11.4),
      new FoodMaterial("Melon", 10.2),
    )
  private val foodMaterials2: List[FoodMaterial] =
    List[FoodMaterial](new FoodMaterial("Strawberry", 10.26), new FoodMaterial("Strawberry", 2.2))
  private val foodMaterials3: List[FoodMaterial] =
    List[FoodMaterial](new FoodMaterial("Banana", 12.50), new FoodMaterial("Apple", 9.99))

  private val channel1 = new Channel("general")

  private val allChannels: List[Channel] = List(channel1)

  private val party1 = new Farmer(network, allChannels, foodMaterials1, initialBalance = 1000)
  private val party3 =
    new Regulator(network, allChannels, List.empty, initialBalance = 10000, capacity = 2)
  private val party2 =
    new Distributor(
      network,
      allChannels,
      List.empty,
      initialBalance = 5000,
      capacity = 10,
      distributionLimit = 2,
    )
  private val party4 = new Customer(network, allChannels, List.empty, initialBalance = 10000)

  val allParties = List(party1, party2, party3, party4)

  def simulationCycle(): Unit = {
    currentTick += 1
    println(s"--- TICK ${currentTick.toString()} ---")

    val allMessages = network.collectMessages()
    val channelMessages = allChannels.map(c => c.collectMessages())

    allParties.foreach { party =>
      val inbox = allMessages(party) ++ channelMessages.flatMap(ch => ch(party))
      party.act(inbox.sortBy(_.timestamp))
    }

    println(s"--------------\n")
  }

  def run(): Unit = {
    val parties = List(party1, party2, party3, party4)
    val initialState = parties.flatMap { party =>
      party.getFoodMaterials().map(new Utxo[FoodMaterial](party, _)) :+
        new Utxo[Money](
          party,
          new Money(party.balance),
        )
    }

    allParties.foreach { node =>
      node.initializeUtxos(initialState)
      network.registerNode(node)
      channel1.registerParty(node)
    // channel2.registerParty(node)
    }
    println("Starting simulation")

    println(party1.getUtxos().map(_.toString()).mkString("\n"))
    // println(party2.getUtxos())

    0 until 20 foreach { _ =>
      simulationCycle()
    // Thread.sleep(1000)
    }
    println(party1.getUtxos().map(_.toString()).mkString("\n"))
    // println(party2.getUtxos())
  }
}
