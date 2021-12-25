package cz.cvut.fel.omo.foodchain.blockchain.security

import scala.collection.mutable.ListBuffer
import cz.cvut.fel.omo.foodchain.Logger

trait SecurityLog {
  def report(violation: BlockChainViolation): Unit
  def getViolations(): List[BlockChainViolation]
}

class InMemorySecurityLog extends SecurityLog {
  private val violations: ListBuffer[BlockChainViolation] = ListBuffer.empty[BlockChainViolation]
  def report(violation: BlockChainViolation): Unit = {
    val _ = violations.append(violation)
  }

  def getViolations(): List[BlockChainViolation] = violations.toList
}
