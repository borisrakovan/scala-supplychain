package cz.cvut.fel.omo.foodchain.foodchain.parties

import cz.cvut.fel.omo.foodchain.blockchain.Network
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.foodchain.Message
import cz.cvut.fel.omo.foodchain.foodchain.channels.Channel

class Customer(
    network: Network,
    channels: List[Channel],
    foodMaterials: List[FoodMaterial],
    initialBalance: Double,
  ) extends FoodChainParty("customer", network, channels, foodMaterials, initialBalance) {
  override def act(inbox: List[Message]): Unit =
    super.act(inbox)
}
