package cz.cvut.fel.omo.foodchain.foodchain.parties

import cz.cvut.fel.omo.foodchain.blockchain.Network
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.foodchain.Message
import cz.cvut.fel.omo.foodchain.foodchain.channels.Channel
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterialState
import cz.cvut.fel.omo.foodchain.foodchain.operations.ImportOperation

class Importer(
    network: Network,
    channels: List[Channel],
    foodMaterials: List[FoodMaterial],
    initialBalance: Double,
    capacity: Int,
    importLimit: Int,
  ) extends FoodChainParty(
      "importer",
      network,
      channels,
      foodMaterials,
      initialBalance,
      capacity,
    ) {
  override def act(inbox: List[Message]): Unit = {
    super.act(inbox)

    val waitingMaterials = foodRepo.getInState(FoodMaterialState.Waiting)
    val materialsToProcess = waitingMaterials.slice(0, importLimit)

    if (materialsToProcess.length > 0) {
      val op = new ImportOperation(
        materialsToProcess,
        party = this,
      )

      materialsToProcess.foreach(processMaterial(_, op))
      recordOperation(op)
    }
  }
}
