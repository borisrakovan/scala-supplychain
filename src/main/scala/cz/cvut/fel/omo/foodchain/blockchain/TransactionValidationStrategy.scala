package cz.cvut.fel.omo.foodchain.blockchain

trait TransactionValidationStrategy {
  def validate(
      tx: Transaction[Node, UtxoContent, Operation[UtxoContent]]
    ): Boolean
}

// server as an example implementation
class BitcoinValidationStrategy extends TransactionValidationStrategy {
  override def validate(
      tx: Transaction[Node, UtxoContent, Operation[UtxoContent]]
    ): Boolean = {
    val inputContents = tx.inputs.map(_.utxo.content.id)
    tx.outputs.find(utxo => !inputContents.contains(utxo.content.id)).isEmpty
  }
}
