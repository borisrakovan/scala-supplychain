package cz.cvut.fel.omo.foodchain.blockchain

import cz.cvut.fel.omo.foodchain.common.Transferable

trait Network {
  def broadcast(msg: Transferable, sender: Node): Unit
  def getParticipants(): List[Node]
}
