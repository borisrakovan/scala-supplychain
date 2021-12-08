package cz.cvut.fel.omo.foodchain.foodchain.parties

import cz.cvut.fel.omo.foodchain.blockchain.Network
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.common.Message
import cz.cvut.fel.omo.foodchain.foodchain.channels.Channel

class Distributor(
    network: Network[FoodChainParty],
    channels: List[Channel],
    foodMaterials: List[FoodMaterial],
    initialBalance: Double,
  ) extends FoodChainParty("distributor", network, channels, foodMaterials, initialBalance) {
  override def act(inbox: List[Message[FoodChainParty]]): Unit = {
    super.act(inbox)

    if (currentTick == 4) {
      println(getUtxos())
      val regulator = network.getById("regulator")

      transferFoodMaterial(foodMaterials(0), regulator)
      makePayment(5050, regulator)

    }
  }
}
