package cz.cvut.fel.omo.foodchain.blockchain

object BlockChain {
  val BlockSize: Int = 2
}

class BlockChain[N <: Node, O <: Operation[UtxoContent]](val history: List[Block]) {
  def appendTransactions(transactions: List[Transaction[N, UtxoContent, O]]): Option[Block] = {
    val newBlock =
      new Block(history.lastOption.map(_.hash).getOrElse(""), transactions, 1)

    append(newBlock) match {
      case Some(value) => Some(newBlock)
      case None => None
    }
  }

  def append(block: Block): Option[BlockChain[N, O]] = {
    val newBlockChain = new BlockChain[N, O](history :+ block)
    this.history.lastOption match {
      case Some(lastBlock) =>
        val hashValid = block.prevBlockHash == lastBlock.hash
        val timestampValid = block.timestamp >= lastBlock.timestamp

        hashValid && timestampValid match {
          case true => Some(newBlockChain)
          case false =>
            println(hashValid, timestampValid)
            println(block.timestamp)
            println(lastBlock.timestamp)
            None
        }

      case None => Some(newBlockChain)
    }
  }
}
