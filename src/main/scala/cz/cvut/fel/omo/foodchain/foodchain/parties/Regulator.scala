package cz.cvut.fel.omo.foodchain.foodchain.parties

import cz.cvut.fel.omo.foodchain.blockchain.Network
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.common.Message
import cz.cvut.fel.omo.foodchain.foodchain.channels.Channel
import cz.cvut.fel.omo.foodchain.foodchain.operations.InspectionOperation
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterialState

class Regulator(
    network: Network,
    channels: List[Channel],
    foodMaterials: List[FoodMaterial],
    initialBalance: Double,
    capacity: Int,
  ) extends FoodChainParty(
      "regulator",
      network,
      channels,
      foodMaterials,
      initialBalance,
      capacity,
    ) {
  override def act(inbox: List[Message]): Unit = {
    super.act(inbox)

    val materialToProcess = foodRepo.getInState(FoodMaterialState.Waiting).headOption

    materialToProcess match {
      case Some(m) =>
        val op = new InspectionOperation(
          material = m,
          party = this,
        )
        processMaterial(m)
        recordOperation(op)
      case None =>
    }
  }
}
