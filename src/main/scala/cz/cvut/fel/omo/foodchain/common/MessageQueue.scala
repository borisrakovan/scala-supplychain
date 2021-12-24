package cz.cvut.fel.omo.foodchain.common

import cz.cvut.fel.omo.foodchain.blockchain.Node

trait Transferable

class Message(
    val sender: Node,
    val recipient: Node,
    val content: Transferable,
  ) {
  val timestamp: Long = System.currentTimeMillis
}

trait MessageQueue {
  def collectMessages(): Map[Node, List[Message]]
}
