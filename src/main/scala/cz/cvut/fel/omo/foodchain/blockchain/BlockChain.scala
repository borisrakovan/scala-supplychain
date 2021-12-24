package cz.cvut.fel.omo.foodchain.blockchain

import scala.util.Try
import cz.cvut.fel.omo.foodchain.blockchain.security.AlreadyMinedException
import cz.cvut.fel.omo.foodchain.blockchain.security.BlockChainException

class BlockChain(val history: List[Block]) {
  def numBlocks(): Int = history.size

  def flatTransactions(): List[Transaction[UtxoContent]] = history.flatMap(_.transactions)

  def getLastHash(): String = history.lastOption.map(_.hash).getOrElse("")

  def append(block: Block): Try[BlockChain] = Try {
    val newBlockChain = new BlockChain(history :+ block)
    this.history.lastOption match {
      case Some(lastBlock) =>
        if (lastBlock.time == block.time)
          throw new AlreadyMinedException

        val hashValid = block.prevBlockHash == lastBlock.hash
        val timestampValid = block.timestamp >= lastBlock.timestamp

        if (!hashValid)
          throw new BlockChainException(
            s"Hashes do not match: ${block.prevBlockHash} ${lastBlock.hash}"
          )
        if (!timestampValid)
          throw new BlockChainException(
            s"Timestamps are not valid: ${block.timestamp} ${lastBlock.timestamp}"
          )
        newBlockChain

      case None => newBlockChain
    }
  }

  override def toString(): String = history.map(_.toString()).mkString(", ")
}
