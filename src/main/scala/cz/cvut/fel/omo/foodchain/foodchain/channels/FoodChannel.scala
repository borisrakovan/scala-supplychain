package cz.cvut.fel.omo.foodchain.foodchain.channels

import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty

abstract class FoodChannel(val name: String) extends Channel
final case class MeatChannel() extends FoodChannel("meat")
final case class VegetableChannel() extends FoodChannel("vegetable")
final case class FruitChannel() extends FoodChannel("fruit")
final case class DairyChannel() extends FoodChannel("dairy")
final case class GrainChannel() extends FoodChannel("grain")
final case class FishChannel() extends FoodChannel("fish")

class FoodMaterialRequest(
    channel: FoodChannel,
    sender: FoodChainParty,
    val material: FoodMaterial,
    val amount: Int,
  ) extends ChannelRequest(channel, sender) {}
