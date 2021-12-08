package cz.cvut.fel.omo.foodchain.blockchain

import cz.cvut.fel.utils.IdGenerator

/** Unspent transaction output */
class Utxo[+U <: UtxoContent](val owner: Node, val content: U) {
  def validateSignature(signature: String): Boolean = owner.sign(this) == signature
  override def equals(other: Any): Boolean =
    other match {
      case o: Utxo[UtxoContent] => content.id.equals(o.content.id) && owner.id == o.owner.id
      case _ => false
    }

  override def hashCode(): Int = (41 * (41 + content.id.##)) + owner.id.##

  override def toString(): String = s"<Utxo content=${content.toString()} owner=${owner.id}>"
}

object UtxoIdGenerator extends IdGenerator {
  override def getNextId(): String = {
    val id = super.getNextId()
    s"utxo_$id"
  }
}

trait UtxoContent {
  val id: String = UtxoIdGenerator.getNextId()
}
