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
import cz.cvut.fel.omo.foodchain.foodchain.parties.Importer
import cz.cvut.fel.omo.foodchain.foodchain.parties.Retailer
import cz.cvut.fel.omo.foodchain.utils.Utils
import cz.cvut.fel.omo.foodchain.foodchain.HostileBehaviour

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

    parties.foreach { party =>
      party.initializeState(initialState)
      network.registerNode(party)
      channels.foreach(_.registerParty(party))
    }

    new Ecosystem(network, parties, channels, foodMaterials)
  }

  private def createParties(
      network: EcosystemNetwork,
      channels: List[Channel],
      foodMaterials: List[FoodMaterial],
    ): List[FoodChainParty] =
    List(
      new Farmer(network, channels, foodMaterials, 1000),
      new Importer(network, channels, List.empty, 1000, capacity = 10, importLimit = 4),
      new Regulator(network, channels, List.empty, 10000, capacity = 5),
      new Distributor(network, channels, List.empty, 5000, capacity = 10, distributionLimit = 4),
      new Retailer(network, channels, List.empty, 20000, capacity = 100, materialLimit = 60),
      new Customer(network, channels, List.empty, 10000),
    )

  private def createChannels(): List[Channel] =
    // currently, there is only 1 channel for simplicity
    // if we want to add more channels, we would have to make sure that there are representatives of all party
    //  types in order for the supply chain to work correctly
    List(
      new Channel("general")
    )

  def createFoodMaterials(): List[FoodMaterial] = {
    val minPrice = 1.0
    val maxPrice = 100.0
    def r() = Utils.random(minPrice, maxPrice)

    List(
      new FoodMaterial("apple", r()),
      new FoodMaterial("pear", r()),
      new FoodMaterial("melon", r()),
      new FoodMaterial("strawberry", r()),
      new FoodMaterial("banana", r()),
      new FoodMaterial("tomato", r()),
      new FoodMaterial("potato", r()),
      new FoodMaterial("cucumber", r()),
      new FoodMaterial("salad", r()),
      new FoodMaterial("cabbage", r()),
      new FoodMaterial("egg", r()),
      new FoodMaterial("milk", r()),
      new FoodMaterial("cheesse", r()),
      new FoodMaterial("suggar", r()),
      new FoodMaterial("salt", r()),
      new FoodMaterial("chocolate", r()),
      new FoodMaterial("apple pie", r()),
      new FoodMaterial("fries", r()),
    )
  }
}
