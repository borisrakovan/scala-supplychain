package cz.cvut.fel.omo.foodchain.common

import cz.cvut.fel.omo.foodchain.blockchain.Node

trait Transferable

class Message[N <: Node](
    val sender: N,
    val recipient: N,
    val content: Transferable,
  )

trait MessageQueue[N <: Node] {
  def collectMessages(): Map[N, List[Message[N]]]
}
