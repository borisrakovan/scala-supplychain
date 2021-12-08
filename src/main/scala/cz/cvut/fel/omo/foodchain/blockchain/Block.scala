package cz.cvut.fel.omo.foodchain.blockchain

import cz.cvut.fel.omo.foodchain.common.Transferable

class Block(
    val prevBlock: Option[Block],
    val transactions: List[Transaction[Node, UtxoContent, Operation[UtxoContent]]],
    val nonce: Int,
  ) extends Transferable {
  val timestamp: Long = System.currentTimeMillis
  def hash(): String = ""

  override def toString: String = hash()
}
