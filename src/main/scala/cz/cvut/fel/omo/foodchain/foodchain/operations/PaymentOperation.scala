package cz.cvut.fel.omo.foodchain.foodchain.operations

import cz.cvut.fel.omo.foodchain.blockchain.Utxo
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import cz.cvut.fel.omo.foodchain.blockchain.Operation
import cz.cvut.fel.omo.foodchain.foodchain.Money

class PaymentOperation(
    val amount: Double,
    val from: FoodChainParty,
    val to: FoodChainParty,
  ) extends Operation[Money] {
  val inputs = getNeededMoneyUtxos(amount)
  val totalInputAmount = inputs.map(_.content.amount).sum

  val outputs = List(
    new Utxo(owner = to, new Money(amount)),
    new Utxo(owner = from, new Money(totalInputAmount - amount)),
  )

  override def getInputs(): List[Utxo[Money]] = inputs
  override def getOutputs(): List[Utxo[Money]] = outputs

  def getAmount(): Double = outputs.filter(_.owner == to).map(_.content.amount).sum

  private def getNeededMoneyUtxos(
      amount: Double
    ): List[Utxo[Money]] = {
    println("^" * 10)
    println(from.getLiveOwnedUtxos())
    println("^" * 10)
    val myMoney: List[Utxo[Money]] = from.getLiveOwnedUtxos().flatMap { utxo =>
      utxo.content match {
        case _: Money => Some(utxo.asInstanceOf[Utxo[Money]])
        case _ => None
      }
    }
    takeWhileLessThan(
      amount,
      myMoney,
      (x: Utxo[Money]) => x.content.amount,
      List.empty[Utxo[Money]],
    ) match {
      case Some(x) => x
      case None => throw new RuntimeException("Not enough money in utxos")
    }
  }

  @annotation.tailrec
  private def takeWhileLessThan[A](
      bound: Double,
      source: List[A],
      getValue: A => Double,
      acc: List[A],
    ): Option[List[A]] =
    if (bound <= 0)
      Some(acc)
    else
      source.headOption match {
        case Some(head) =>
          takeWhileLessThan(bound - getValue(head), source.drop(1), getValue, head :: acc)
        case None => None
      }

  override def toString(): String = s"${super.toString()}(${from.toString()} -> ${to.toString()})"
}
