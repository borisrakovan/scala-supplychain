package cz.cvut.fel.omo.foodchain.blockchain

trait TransactionValidationStrategy {
  def validate(
      tx: Transaction[UtxoContent]
    ): Boolean
}
