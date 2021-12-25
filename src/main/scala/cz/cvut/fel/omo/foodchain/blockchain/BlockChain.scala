package cz.cvut.fel.omo.foodchain.blockchain

import scala.util.Try
import cz.cvut.fel.omo.foodchain.blockchain.security.AlreadyMinedException
import cz.cvut.fel.omo.foodchain.blockchain.security.BlockChainException
import cz.cvut.fel.omo.foodchain.blockchain.security.BlockMutationViolation
import cz.cvut.fel.omo.foodchain.blockchain.consensus.ProofOfWork
import cz.cvut.fel.omo.foodchain.utils.Crypto

class BlockChain(val history: List[Block]) {
  def numBlocks(): Int = history.size

  def flatTransactions(): List[Transaction[UtxoContent]] = history.flatMap(_.transactions)

  def getLastHash(): String = history.lastOption.map(_.hash).getOrElse("")

  def append(block: Block): Try[BlockChain] = Try {
    val newBlockChain = new BlockChain(history :+ block)
    // Check if the previous block referenced by the block exists and is valid
    this.history.lastOption match {
      case Some(lastBlock) =>
        if (lastBlock.time == block.time)
          throw new AlreadyMinedException

        val hashValid = block.prevBlockHash == lastBlock.hash

        if (!hashValid)
          throw new BlockChainException(
            s"Hashes do not match: ${block.prevBlockHash} ${lastBlock.hash}"
          )

        // Check that the timestamp of the block is greater than that of the previous
        val timestampValid = block.timestamp >= lastBlock.timestamp

        if (!timestampValid)
          throw new BlockChainException(
            s"Timestamps are not valid: ${block.timestamp} ${lastBlock.timestamp}"
          )
        newBlockChain

      case None => newBlockChain
    }
  }

  def validate(node: Node): Option[BlockMutationViolation] = {
    @scala.annotation.tailrec
    def helper(blocks: List[Block]): Option[Block] =
      blocks match {
        case head :: rest =>
          val trueHash = Crypto.sha256Hash(
            ProofOfWork.makeDescriptor(head.prevBlockHash, head.transactions, head.nonce)
          )
          if (head.hash != trueHash)
            Some(head)
          else
            helper(rest)
        case Nil => None
      }

    helper(history) match {
      case Some(block) =>
        Some(
          new BlockMutationViolation(node, block.time, block)
        )
      case None => None
    }
  }

  override def toString(): String = history.map(_.toString()).mkString(", ")
}
