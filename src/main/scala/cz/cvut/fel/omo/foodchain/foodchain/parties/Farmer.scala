package cz.cvut.fel.omo.foodchain.foodchain.parties

import cz.cvut.fel.omo.foodchain.blockchain.Network
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.common.Message
import cz.cvut.fel.omo.foodchain.foodchain.channels.Channel
import cz.cvut.fel.omo.foodchain.foodchain.operations.HarvestOperation
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterialState

class Farmer(
    network: Network,
    channels: List[Channel],
    foodMaterials: List[FoodMaterial],
    initialBalance: Double,
    harvestLimit: Int = 2,
  ) extends FoodChainParty("farmer", network, channels, foodMaterials, initialBalance) {
  override def act(inbox: List[Message]): Unit = {

    super.act(inbox)
    val waitingMaterials = foodRepo.getInState(FoodMaterialState.Waiting)
    val materialsToProcess = waitingMaterials.slice(0, harvestLimit)

    if (materialsToProcess.length > 0) {
      val op = new HarvestOperation(
        materialsToProcess,
        party = this,
      )

      materialsToProcess.foreach(processMaterial(_, op))
      recordOperation(op)
    }
  }
}
