package cz.cvut.fel.omo.foodchain.blockchain

import scala.collection.mutable
import cz.cvut.fel.omo.foodchain.common.{ Message, MessageQueue, Transferable }

// [OBSERVER] Subject / ConcreteSubject
class Network[N <: Node] extends MessageQueue[N] {
  // TODO: work only with ids, not with actual instances. ids will be loaded from config. move network directly to Node
  type TX = Transaction[Node, UtxoContent, Operation[UtxoContent]]

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var nodes: List[N] = List.empty[N]

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val messageQueue = mutable.Queue[Message[N]]()

  def collectMessages(): Map[N, List[Message[N]]] = {
    // println(messageQueue)
    // println(messages)
    val messages = nodes.map { n =>
      (n -> messageQueue.filter(m => m.recipient == n).toList)
    }.toMap
    messageQueue.clear()
    messages
  }

  def getById(id: String): N = nodes.find(n => n.id == id).get

  def registerNode(node: N): Unit = nodes = nodes :+ node

  def removeNode(node: N): Unit = nodes = nodes.filterNot(_ == node)

  def broadcast(msg: Transferable, sender: N): Unit =
    nodes.foreach(node => messageQueue.enqueue(new Message(sender, node, msg)))
}
