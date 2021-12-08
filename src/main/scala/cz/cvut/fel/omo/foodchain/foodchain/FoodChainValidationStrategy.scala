package cz.cvut.fel.omo.foodchain.foodchain

import cz.cvut.fel.omo.foodchain.blockchain._
import cz.cvut.fel.omo.foodchain.foodchain.transactions._

class FoodChainValidationStrategy extends TransactionValidationStrategy {
  private def validateFoodMaterialTransaction(
      tx: FoodTransaction[Node]
    ): Boolean = {
    println("validating food material")
    val inputContents = tx.inputs.map(_.utxo.content.id)
    tx.outputs
      .find { utxo =>
        !inputContents.contains(utxo.content.id)
      }
      .isEmpty
  }

  private def validateMoneyTransaction(
      tx: MoneyTransaction[Node]
    ): Boolean = {
    println("validating money transaction")
    val a = tx.inputs.map(_.utxo.content.amount)
    println(a)
    true
  }

  override def validate(
      tx: Transaction[Node, UtxoContent, Operation[UtxoContent]]
    ): Boolean =
    tx match {
      case tx: MoneyTransaction[Node] =>
        validateMoneyTransaction(tx)
      case tx: FoodTransaction[Node] =>
        validateFoodMaterialTransaction(tx)
      // case tx: Transaction[Node, FoodMaterial, Operation[FoodMaterial]] =>
      //   validateFoodMaterialTransaction(tx)
      case _ => throw new RuntimeException("Received unsupported type of UtxoContent")

    }
  // tx.outputs match {
  //   case head :: _ =>
  //     head.content match {
  //       case c: FoodMaterial => validateFoodMaterialTransaction(tx)
  //       case c: Money => validateMoneyTransaction(tx)
  //     }
  //   case Nil => true
  // }
}
