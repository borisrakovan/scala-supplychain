package cz.cvut.fel.omo.foodchain.blockchain

import cz.cvut.fel.omo.foodchain.common.Transferable

class Block(
    val prevBlockHash: String,
    val transactions: List[Transaction[UtxoContent]],
    val nonce: Long,
    val hash: String,
    val time: Long,
  ) extends Transferable {
  val timestamp: Long = System.currentTimeMillis

  override def toString: String = s"Block(${hash.takeRight(4)})"
}
