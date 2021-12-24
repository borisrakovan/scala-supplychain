package cz.cvut.fel.omo.foodchain.foodchain

import cz.cvut.fel.omo.foodchain.blockchain.UtxoContent

class Money(val amount: Double) extends UtxoContent {
  override def toString(): String =
    s"Money(id=$id, amount=${amount.toString()})"
}
