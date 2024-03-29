package cz.cvut.fel.omo.foodchain.foodchain

import cz.cvut.fel.omo.foodchain.blockchain._
import cz.cvut.fel.omo.foodchain.foodchain.transactions._
import cz.cvut.fel.omo.foodchain.utils.Utils

object FoodChainValidationStrategy {
  val FloatErrorThreshold: Double = 0.001
}
class FoodChainValidationStrategy extends TransactionValidationStrategy {
  private def validateFoodMaterialTransaction(
      tx: FoodTransaction
    ): Boolean = {
    val inputContents = tx.inputs.map(_.utxo.content.id)
    // If the outputs contain an UTXO that is not in the inputs, return an error.
    tx.outputs
      .find { utxo =>
        !inputContents.contains(utxo.content.id)
      }
      .isEmpty
  }

  private def validateMoneyTransaction(
      tx: MoneyTransaction
    ): Boolean = {
    // If the sum of the denominations of all input UTXO is less than the sum of the denominations of all output UTXO, return an error.
    val inputAmount: Double = tx.inputs.map(_.utxo.content.amount).sum
    val outputAmount: Double = tx.outputs.map(_.content.amount).sum

    inputAmount - outputAmount < FoodChainValidationStrategy.FloatErrorThreshold
  }

  override def validate(
      tx: Transaction[UtxoContent]
    ): Boolean =
    tx match {
      case tx: MoneyTransaction =>
        validateMoneyTransaction(tx)
      case tx: FoodTransaction =>
        validateFoodMaterialTransaction(tx)
      // case tx: Transaction[Node, FoodMaterial, Operation[FoodMaterial]] =>
      //   validateFoodMaterialTransaction(tx)
      case _ =>
        Utils.assertionFailed("Received unsupported type of UtxoContent", forceError = true)
        false
    }
}
