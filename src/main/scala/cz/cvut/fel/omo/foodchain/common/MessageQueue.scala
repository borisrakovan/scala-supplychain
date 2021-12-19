package cz.cvut.fel.omo.foodchain.common

import cz.cvut.fel.omo.foodchain.blockchain.Node
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty

trait Transferable

class Message(
    val sender: Node,
    val recipient: Node,
    val content: Transferable,
  )

trait MessageQueue {
  def collectMessages(): Map[Node, List[Message]]
}
