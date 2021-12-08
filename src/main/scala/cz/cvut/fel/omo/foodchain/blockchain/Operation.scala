package cz.cvut.fel.omo.foodchain.blockchain

trait Operation[+U <: UtxoContent] {
  def getInputs(): List[Utxo[U]]
  def getOutputs(): List[Utxo[U]]
}
