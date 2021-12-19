package cz.cvut.fel.omo.foodchain.foodchain

import cz.cvut.fel.omo.foodchain.blockchain._
import cz.cvut.fel.omo.foodchain.foodchain.transactions._

class FoodChainValidationStrategy extends TransactionValidationStrategy {
  private def validateFoodMaterialTransaction(
      tx: FoodTransaction
    ): Boolean = {
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
    val inputAmount = tx.inputs.map(_.utxo.content.amount).sum
    val outputAmount = tx.outputs.map(_.content.amount).sum

    return outputAmount <= inputAmount
  }

  override def validate(
      tx: Transaction[Node, UtxoContent, Operation[UtxoContent]]
    ): Boolean =
    tx match {
      case tx: MoneyTransaction[Node] =>
        validateMoneyTransaction(tx)
      case tx: FoodTransaction =>
        validateFoodMaterialTransaction(tx)
      // case tx: Transaction[Node, FoodMaterial, Operation[FoodMaterial]] =>
      //   validateFoodMaterialTransaction(tx)
      case _ => throw new RuntimeException("Received unsupported type of UtxoContent")

    }
}
