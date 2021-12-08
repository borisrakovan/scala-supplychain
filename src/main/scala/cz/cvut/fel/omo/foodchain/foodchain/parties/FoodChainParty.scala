package cz.cvut.fel.omo.foodchain.foodchain

import cz.cvut.fel.omo.foodchain.blockchain._
import cz.cvut.fel.omo.foodchain.common.{ Message, MessageQueue, Transferable }
import scala.collection.mutable.ListBuffer
import cz.cvut.fel.omo.foodchain.foodchain.channels.ChannelRequest
import cz.cvut.fel.omo.foodchain.foodchain.channels.FoodMaterialRequest
import cz.cvut.fel.omo.foodchain.foodchain.channels.PaymentRequest
import cz.cvut.fel.omo.foodchain.foodchain.transactions._
import cz.cvut.fel.omo.foodchain.foodchain.channels.Channel

abstract class FoodChainParty(
    val id: String,
    val network: Network[FoodChainParty],
    val channels: List[Channel],
    initialMaterials: List[FoodMaterial],
    val initialBalance: Double,
  ) extends Node {
  override val transactionValidationStrategy = new FoodChainValidationStrategy
  val pricingStrategy = new DefaultPricingStrategy
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  override def broadcastBlock(block: Block): Unit = network.broadcast(block, this)
  override def broadcastTransaction(
      tx: Transaction[Node, UtxoContent, Operation[UtxoContent]]
    ): Unit = network.broadcast(tx, this)

  // TODO : read https://www.freecodecamp.org/news/how-to-build-a-simple-actor-based-blockchain-aac1e996c177/
  var currentTick: Int = 0
  val foodMaterials: ListBuffer[FoodMaterial] = ListBuffer.empty[FoodMaterial] ++ initialMaterials
  var balance: Double = initialBalance

  def handleTransaction(tx: TX): Unit =
    tx.operation match {
      case op: TransferOperation =>
        if (op.to == this) {
          foodMaterials ++= op.materials
          ()
        }
      case op: PaymentOperation =>
        if (op.to == this)
          balance += op.getAmount()
      case op => throw new RuntimeException(s"Unknown operation ${op.toString()}")
    }

  def handleChannelRequest(request: ChannelRequest): Unit =
    request match {
      case req: FoodMaterialRequest =>
        val myMaterials = foodMaterials.filter(_.name == req.material.name)
        if (myMaterials.size < req.amount)
          log("can no longer satisfy incoming request")
        // TODO: send request back to channel
        else {
          val materials = myMaterials.take(req.amount)
          sellFoodMaterials(materials.toList, to = req.sender)
        }
      case req: PaymentRequest =>
        if (balance < req.amount)
          throw new RuntimeException(s"Not enough money to pay ${req.amount}")
        else if (req.payer != this)
          throw new RuntimeException(s"Received payment request meant for someone else")
        else
          makePayment(req.amount, req.sender)

      case _ => throw new RuntimeException(s"Unknown request ${request.toString()}")
    }

  def offerChannelRequest(request: ChannelRequest): Boolean = {
    log("offered channel request")
    request match {
      case req: FoodMaterialRequest =>
        val myMaterials = foodMaterials.filter(_.name == req.material.name)
        if (myMaterials.size >= req.amount) {
          log("yes sir!")
          true
        }
        else {
          log("not enough materials")
          false
        }
      case req: PaymentRequest => req.payer == this
      case _ => throw new RuntimeException(s"Unknown channel request: ${request.toString()}")
    }
  }

  def act(inbox: List[Message[FoodChainParty]]): Unit = {
    currentTick += 1

    inbox.foreach { message =>
      message.content match {
        case tx: TX =>
          println("tx")
          val _ = receiveTransaction(tx)
          handleTransaction(tx)

        case b: Block =>
          println("block")
          val _ = receiveBlock(b)

        case req: ChannelRequest =>
          println("channel request")
          handleChannelRequest(req)
        case _ =>
          throw new RuntimeException(s"Unknown message content: ${message.content.toString()}")
      }
    }
  }

  protected def sellFoodMaterials(materials: List[FoodMaterial], to: FoodChainParty): Unit = {
    materials.foreach(m => m.price = pricingStrategy.apply(m))
    transferFoodMaterials(materials, to)
    val totalPrice = materials.map(_.getPrice()).sum
    requestPayment(totalPrice, from = to)
  }
  // requestPayment()

  protected def transferFoodMaterials(materials: List[FoodMaterial], to: FoodChainParty): Unit = {
    log(s"Transferring ${materials.toString()} to ${to.id}")

    val transferOperation = new TransferOperation(materials, from = this, to = to)
    val tx = new FoodTransaction[FoodChainParty](transferOperation, initiator = this)

    broadcastTransaction(tx)
    foodMaterials --= materials
  }

  protected def transferFoodMaterial(material: FoodMaterial, to: FoodChainParty): Unit =
    transferFoodMaterials(List(material), to)

  protected def requestPayment(amount: Double, from: FoodChainParty): Unit =
    // TODO: somehow get the appropriate channel and send payment request
    log(s"Requesting payment of ${amount.toString()} to ${from.id}")

  protected def makePayment(amount: Double, to: FoodChainParty): Unit = {
    log(s"Paying ${amount.toString()} to ${to.id}")

    if (amount > balance)
      // TODO
      throw new RuntimeException(s"Not enough money to pay ${amount.toString()} to ${to.id}")

    val inputUtxos = getNeededMoneyUtxos(amount)
    val totalInputAmount = inputUtxos.map(_.content.amount).sum

    val outputUtxos = List(
      new Utxo(to, new Money(amount)),
      new Utxo(this, new Money(totalInputAmount - amount)),
    )

    val paymentOperation = new PaymentOperation(inputUtxos, outputUtxos, from = this, to = to)

    log(paymentOperation.getInputs().toString())
    val tx = new MoneyTransaction[FoodChainParty](
      paymentOperation,
      initiator = this,
    )

    broadcastTransaction(tx)
    balance -= amount
  }

  private def getNeededMoneyUtxos(
      amount: Double
    ): List[Utxo[Money]] = {
    val myMoney: List[Utxo[Money]] = getOwnedUtxos().flatMap { utxo =>
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
}
// class Producer extends FoodChainParty
// class Importer extends FoodChainParty
// class Supplier extends FoodChainParty
// class Regulator extends FoodChainParty
// class Retailer extends FoodChainParty
// class Customer extends FoodChainParty
