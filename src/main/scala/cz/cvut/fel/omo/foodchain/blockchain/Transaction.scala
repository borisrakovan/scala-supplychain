package cz.cvut.fel.omo.foodchain.blockchain

import cz.cvut.fel.omo.foodchain.common.Transferable
import cz.cvut.fel.utils.IdGenerator

class TransactionInput[+U <: UtxoContent](
    val utxo: Utxo[U],
    val signature: String,
  )

object Transaction {
  private val idGen = new IdGenerator

  def nextId(): String = idGen.getNextId()
}

class Transaction[+U <: UtxoContent](
    val operation: Operation[U],
    val initiator: Node,
    val time: Long,
  ) extends Transferable {
  val id = Transaction.nextId()

  val outputs: List[Utxo[U]] = operation.getOutputs()
  val inputs: List[TransactionInput[U]] = operation.getInputs().map { utxo =>
    val signature = initiator.sign(utxo)
    new TransactionInput(utxo, signature)
  }

  override def toString(): String = this.getClass().getSimpleName()
}
