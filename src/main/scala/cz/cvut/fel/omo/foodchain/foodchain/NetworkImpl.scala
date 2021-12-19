package cz.cvut.fel.omo.foodchain.foodchain

import cz.cvut.fel.omo.foodchain.blockchain.Transaction
import scala.collection.mutable
import cz.cvut.fel.omo.foodchain.common.{ Message, MessageQueue, Transferable }
import cz.cvut.fel.omo.foodchain.blockchain.Node
import cz.cvut.fel.omo.foodchain.blockchain.UtxoContent
import cz.cvut.fel.omo.foodchain.blockchain.Operation
import cz.cvut.fel.omo.foodchain.blockchain.Network

class NetworkImpl extends Network with MessageQueue {
  // TODO: work only with ids, not with actual instances. ids will be loaded from config.
  type TX = Transaction[Node, UtxoContent, Operation[UtxoContent]]

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var nodes: List[Node] = List.empty[Node]

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val messageQueue = mutable.Queue[Message]()

  def collectMessages(): Map[Node, List[Message]] = {
    // println(messageQueue)
    // println(messages)
    val messages = nodes.map { n =>
      (n -> messageQueue.filter(m => m.recipient == n).toList)
    }.toMap
    messageQueue.clear()
    messages
  }

  def registerNode(node: Node): Unit = nodes = nodes :+ node

  def removeNode(node: Node): Unit = nodes = nodes.filterNot(_ == node)

  def broadcast(msg: Transferable, sender: Node): Unit =
    nodes.filter(_ != sender).foreach(node => messageQueue.enqueue(new Message(sender, node, msg)))
}
