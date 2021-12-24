package cz.cvut.fel.omo.foodchain.blockchain.security

import cz.cvut.fel.omo.foodchain.blockchain.Transaction
import cz.cvut.fel.omo.foodchain.blockchain.Node
import cz.cvut.fel.omo.foodchain.blockchain.UtxoContent
import cz.cvut.fel.omo.foodchain.blockchain.TransactionInput

abstract class BlockChainViolation(val node: Node, val time: Long) {
  val description: String = ""
  def raiseException(): Nothing = throw new BlockChainException(this.toString())
  override def toString(): String = this.getClass().getSimpleName()
}

class DoubleSpendingViolation(
    node: Node,
    time: Long,
    val tx: Transaction[UtxoContent],
    val txInput: TransactionInput[UtxoContent],
  ) extends BlockChainViolation(node, time) {
  override val description: String =
    s"Utxo ${txInput.utxo.content.toString()} in transaction ${tx.id}"
}

class BlockMutationViolation(node: Node, time: Long) extends BlockChainViolation(node, time)

class SignatureForgeryViolation(
    node: Node,
    time: Long,
    val tx: Transaction[UtxoContent],
    val signature: String,
  ) extends BlockChainViolation(node, time) {
  override val description: String = s"Invalid signature ${signature} in transaction ${tx.id}"
}

class InvalidTransactionViolation(
    node: Node,
    time: Long,
    val tx: Transaction[UtxoContent],
  ) extends BlockChainViolation(node, time) {
  override val description: String =
    s"${tx.toString()} ${tx.id} ${tx.inputs.map(_.utxo.content)} ${tx.outputs.map(_.content)}"
}
