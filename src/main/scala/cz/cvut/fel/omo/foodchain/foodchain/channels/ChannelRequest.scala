package cz.cvut.fel.omo.foodchain.foodchain.channels
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.common.Transferable

abstract class ChannelRequest(val channel: Channel, val sender: FoodChainParty)
    extends Transferable {
  override def toString(): String = this.getClass().getSimpleName()
}

class FoodMaterialSellRequest(
    channel: Channel,
    sender: FoodChainParty,
    val material: FoodMaterial,
  ) extends ChannelRequest(channel, sender) {}

class FoodMaterialBuyRequest(
    channel: Channel,
    sender: FoodChainParty,
    val material: FoodMaterial,
    val seller: FoodChainParty,
  ) extends ChannelRequest(channel, sender) {}

object FoodMaterialBuyRequest {
  def asResponseTo(
      sellReq: FoodMaterialSellRequest,
      buyer: FoodChainParty,
    ): FoodMaterialBuyRequest =
    new FoodMaterialBuyRequest(sellReq.channel, buyer, sellReq.material, sellReq.sender)
}

class PaymentRequest(
    channel: Channel,
    sender: FoodChainParty,
    val amount: Double,
    val payer: FoodChainParty,
  ) extends ChannelRequest(channel, sender) {}
