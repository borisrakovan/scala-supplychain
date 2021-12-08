package cz.cvut.fel.omo.foodchain.blockchain

import cz.cvut.fel.omo.foodchain.common.Transferable

class TransactionInput[+N <: Node, +U <: UtxoContent](
    val utxo: Utxo[U],
    val signature: String,
  )

// class MoneyInputs[+N <: Node](
//     utxo: Utxo[Money],
//     signature: String,
//   ) extends TransactionInput[N, Money](utxo, signature)
// class FoodInputs[+N <: Node](
//     utxo: Utxo[FoodMaterial],
//     signature: String,
//   ) extends TransactionInput[N, FoodMaterial](utxo, signature)

class Transaction[+N <: Node, +U <: UtxoContent, +O <: Operation[U]](
    val operation: O,
    val initiator: N,
  ) extends Transferable {
  private val opInputs: List[Utxo[U]] = operation.getInputs()
  val outputs: List[Utxo[U]] = operation.getOutputs()
  val inputs: List[TransactionInput[N, U]] = opInputs.map { utxo =>
    val signature = initiator.sign(utxo)
    new TransactionInput(utxo, signature)
  }
}
