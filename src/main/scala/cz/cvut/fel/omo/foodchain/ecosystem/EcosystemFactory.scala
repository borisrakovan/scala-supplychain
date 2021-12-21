package cz.cvut.fel.omo.foodchain.ecosystem

import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import cz.cvut.fel.omo.foodchain.foodchain.channels.Channel
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.foodchain.parties.Farmer
import cz.cvut.fel.omo.foodchain.foodchain.parties.Regulator
import cz.cvut.fel.omo.foodchain.foodchain.parties.Distributor
import cz.cvut.fel.omo.foodchain.foodchain.parties.Customer
import cz.cvut.fel.omo.foodchain.foodchain.{ EcosystemNetworkImpl, EcosystemNetwork }
import cz.cvut.fel.omo.foodchain.blockchain.Utxo
import cz.cvut.fel.omo.foodchain.foodchain.Money

trait EcosystemFactory {
  def create(): Ecosystem
}

class ConcreteEcosystemFactory extends EcosystemFactory {
  def create(): Ecosystem = {
    val network = new EcosystemNetworkImpl
    val channels = createChannels()
    val foodMaterials = createFoodMaterials()
    val parties = createParties(network, channels, foodMaterials)

    val initialState = parties.flatMap { party =>
      val initialFood = party.getFoodMaterials().map(new Utxo[FoodMaterial](party, _))
      val initialMoney = new Utxo[Money](
        party,
        new Money(party.balance),
      )
      initialFood :+ initialMoney
    }

    parties.foreach { node =>
      node.initializeUtxos(initialState)
      network.registerNode(node)
      // TODO
      channels.foreach(_.registerParty(node))
    }

    new Ecosystem(network, parties, channels, foodMaterials)
  }

  private def createParties(
      network: EcosystemNetwork,
      channels: List[Channel],
      foodMaterials: List[FoodMaterial],
    ): List[FoodChainParty] = {
    val party1 = new Farmer(network, channels, foodMaterials, initialBalance = 1000)
    val party3 =
      new Regulator(network, channels, List.empty, initialBalance = 10000, capacity = 2)
    val party2 =
      new Distributor(
        network,
        channels,
        List.empty,
        initialBalance = 5000,
        capacity = 10,
        distributionLimit = 2,
      )
    val party4 = new Customer(network, channels, List.empty, initialBalance = 10000)

    List(party1, party2, party3, party4)
  }

  private def createChannels(): List[Channel] =
    List(
      new Channel("general")
    )

  def createFoodMaterials(): List[FoodMaterial] =
    List(
      new FoodMaterial("Apple", 12.04),
      new FoodMaterial("Pear", 11.4),
      new FoodMaterial("Melon", 10.2),
      new FoodMaterial("Strawberry", 10.26),
      new FoodMaterial("Strawberry", 2.2),
    )
}
