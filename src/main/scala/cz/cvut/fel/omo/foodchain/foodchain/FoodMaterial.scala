package cz.cvut.fel.omo.foodchain.foodchain
import cz.cvut.fel.utils.IdGenerator

import cz.cvut.fel.omo.foodchain.blockchain.UtxoContent

trait Tradeable {
  def getPrice(): Double
}
sealed abstract class FoodMaterial(val name: String, var price: Double)
    extends UtxoContent
       with Tradeable {
  override def toString(): String = s"$name(id=$id)"
  override def getPrice(): Double = price
}

final case class Apple(_price: Double) extends FoodMaterial("apple", _price)
final case class Pear(_price: Double) extends FoodMaterial("pear", _price)
final case class Melon(_price: Double) extends FoodMaterial("melon", _price)
final case class Strawberry(_price: Double) extends FoodMaterial("strawberry", _price)
final case class Banana(_price: Double) extends FoodMaterial("banana", _price)
// object FoodMaterial extends UtxoContent {
//   override def isValidDerivation(
//       input: List[UtxoContent],
//       output: List[UtxoContent],
//     ): Boolean = output.find(material => !input.map(m => m.id).contains(material.id)).isEmpty
// }
