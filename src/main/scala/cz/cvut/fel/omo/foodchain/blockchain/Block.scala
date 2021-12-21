package cz.cvut.fel.omo.foodchain.blockchain

import cz.cvut.fel.omo.foodchain.common.Transferable
import cz.cvut.fel.omo.foodchain.utils.Crypto

class Block(
    val prevBlockHash: String,
    val transactions: List[Transaction[Node, UtxoContent, Operation[UtxoContent]]],
    val nonce: Int,
  ) extends Transferable {
  val timestamp: Long = System.currentTimeMillis
  val hash: String = {
    val txHash = transactions.map(_.hashCode()).mkString(":")
    Crypto.sha256Hash(s"${prevBlockHash}:${txHash}:$nonce")
  }

  override def toString: String = s"Block(${hash})"
}
