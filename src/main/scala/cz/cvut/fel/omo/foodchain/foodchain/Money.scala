package cz.cvut.fel.omo.foodchain.foodchain

import cz.cvut.fel.omo.foodchain.blockchain.UtxoContent
import cz.cvut.fel.omo.foodchain.utils.Utils

class Money(val amount: Double) extends UtxoContent {
  override def toString(): String =
    s"Money(id=$id, amount=${Utils.formatPrice(amount)})"
}
