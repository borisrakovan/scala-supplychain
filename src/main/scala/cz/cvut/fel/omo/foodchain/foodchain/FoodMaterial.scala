package cz.cvut.fel.omo.foodchain.foodchain

import cz.cvut.fel.omo.foodchain.blockchain.UtxoContent
trait Tradeable {
  def getPrice(): Double
}

sealed trait FoodMaterialState
object FoodMaterialState {
  case object Waiting extends FoodMaterialState
  case object Processed extends FoodMaterialState
}

class FoodMaterial(val ofType: String, var price: Double) extends UtxoContent with Tradeable {
  override def toString(): String = s"$ofType(id=$id)"
  override def getPrice(): Double = price
}
