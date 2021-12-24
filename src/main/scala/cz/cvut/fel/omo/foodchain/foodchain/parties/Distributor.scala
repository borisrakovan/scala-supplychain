package cz.cvut.fel.omo.foodchain.foodchain.parties

import cz.cvut.fel.omo.foodchain.blockchain.Network
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.common.Message
import cz.cvut.fel.omo.foodchain.foodchain.channels.Channel
import scala.util.Random
import cz.cvut.fel.omo.foodchain.foodchain.operations.DistributionOperation
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterialState

class Distributor(
    network: Network,
    channels: List[Channel],
    foodMaterials: List[FoodMaterial],
    initialBalance: Double,
    capacity: Int,
    distributionLimit: Int,
  ) extends FoodChainParty(
      "distributor",
      network,
      channels,
      foodMaterials,
      initialBalance,
      capacity,
    ) {
  override def act(inbox: List[Message]): Unit = {
    super.act(inbox)

    val waitingMaterials = foodRepo.getInState(FoodMaterialState.Waiting)
    val materialsToProcess = waitingMaterials.slice(0, distributionLimit)

    if (materialsToProcess.length > 0) {
      val op = new DistributionOperation(
        materialsToProcess,
        party = this,
        distance = Random.between(10, 100),
      )

      materialsToProcess.foreach(processMaterial(_, op))
      recordOperation(op)
    }

  }
}
