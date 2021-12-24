package cz.cvut.fel.omo.foodchain.foodchain.parties
import scala.math
import scala.util.Random
import cz.cvut.fel.omo.foodchain.blockchain.Network
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.common.Message
import cz.cvut.fel.omo.foodchain.foodchain.channels.Channel
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterialState
import cz.cvut.fel.omo.foodchain.foodchain.operations.MarketingOperation

class Retailer(
    network: Network,
    channels: List[Channel],
    foodMaterials: List[FoodMaterial],
    initialBalance: Double,
    capacity: Int,
    val materialLimit: Int = Int.MaxValue,
  ) extends FoodChainParty(
      "retailer",
      network,
      channels,
      foodMaterials,
      initialBalance,
      capacity,
    ) {
  override def act(inbox: List[Message]): Unit = {
    super.act(inbox)

    val waitingMaterials = foodRepo.getInState(FoodMaterialState.Waiting)
    val materialsToProcess = waitingMaterials.slice(0, materialLimit)

    val discountPercentage: Option[Double] =
      if (math.random() < 0.5) Some(Random.between(5, 30)) else None

    if (materialsToProcess.length > 0) {
      val op = new MarketingOperation(
        materials = materialsToProcess,
        party = this,
        discountPercentage = discountPercentage,
      )
      materialsToProcess.foreach(processMaterial(_, op))
      recordOperation(op)
    }
  }
}
