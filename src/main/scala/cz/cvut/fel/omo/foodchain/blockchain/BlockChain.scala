package cz.cvut.fel.omo.foodchain.blockchain

class BlockChain[N <: Node, O <: Operation[UtxoContent]](val history: List[Block]) {
  def append(block: Block): Option[BlockChain[N, O]] = {
    val newBlockChain = new BlockChain[N, O](history :+ block)
    this.history.lastOption match {
      case Some(lastBlock) =>
        val hashValid = block.prevBlock match {
          case Some(prevBlock) => prevBlock.hash() == lastBlock.hash()
          case None => true
        }
        val timestampValid = block.timestamp > lastBlock.timestamp

        hashValid && timestampValid match {
          case true => Some(newBlockChain)
          case false => None
        }

      case None => Some(newBlockChain)
    }
  }
}
