package cz.cvut.fel.omo.foodchain.foodchain

import cz.cvut.fel.omo.foodchain.blockchain._
import cz.cvut.fel.omo.foodchain.foodchain.channels._
import cz.cvut.fel.omo.foodchain.foodchain.transactions._
import cz.cvut.fel.utils.IdGenerator
import cz.cvut.fel.omo.foodchain.foodchain.operations._
import scala.collection.mutable.ListBuffer
import cz.cvut.fel.omo.foodchain.Logger
import cz.cvut.fel.omo.foodchain.foodchain.Message
import cz.cvut.fel.omo.foodchain.utils.Utils
import cz.cvut.fel.omo.foodchain.ecosystem.Ecosystem

object PartyIdGenerator extends IdGenerator {}

abstract class FoodChainParty(
    val ofType: String,
    val network: Network,
    val channels: List[Channel],
    initialMaterials: List[FoodMaterial],
    val initialBalance: Double,
    capacity: Int = Int.MaxValue,
  ) extends Node {
  val id = s"$ofType-${PartyIdGenerator.getNextId()}"

  /* type of the previous party in the chain */
  val prevPartyType: Option[String] = Ecosystem.getPreviousParty(forType = ofType)

  override val transactionValidationStrategy = new FoodChainValidationStrategy

  var currentTick: Int = 0
  val foodRepo: FoodRepository = new InMemoryFoodRepository(initialMaterials, capacity)
  var balance: Double = initialBalance
  val expectedMaterials: ListBuffer[FoodMaterial] = ListBuffer.empty[FoodMaterial]

  def handleTransaction(tx: Transaction[UtxoContent]): Unit =
    tx.operation match {
      case op: TransferOperation =>
        if (op.to == this) {
          foodRepo.addMany(op.materials)
          expectedMaterials --= op.materials
        }
      case op: PaymentOperation =>
        if (op.to == this)
          balance += op.getAmount()
      case _ =>
    }

  def handleChannelRequest(request: ChannelRequest): Unit =
    request match {
      case req: FoodMaterialBuyRequest =>
        foodRepo.find(req.material) match {
          case Some(material) =>
            transferFoodMaterials(List(material), to = req.sender)

            Logger.info(
              s"Requesting payment of ${material.price.toString()} from ${req.sender.id}",
              this,
            )
            request
              .channel
              .makeRequest(
                new PaymentRequest(
                  request.channel,
                  sender = this,
                  amount = material.price,
                  payer = req.sender,
                )
              )

          case None =>
            Utils.assertionFailed(s"can no longer satisfy incoming request ${request.toString()}")
        }
      case req: FoodMaterialSellRequest =>
        if (
            prevPartyType.isDefined
            && req.sender.ofType == prevPartyType.get
            && !foodRepo.capacityReached()
            && !expectedMaterials.contains(req.material)
        ) {
          req.channel.makeRequest(FoodMaterialBuyRequest.asResponseTo(req, buyer = this))
          expectedMaterials += req.material
        }

      case req: PaymentRequest =>
        if (balance < req.amount)
          Logger.warn(s"Not enough money to pay ${Utils.formatPrice(req.amount)}", this)
        else if (req.payer != this)
          Utils.assertionFailed(s"Received payment request meant for someone else")
        else
          makePayment(req.amount, req.sender)

      case _ => Utils.assertionFailed(s"Unknown request ${request.toString()}")
    }

  def offerChannelRequest(request: ChannelRequest): Boolean =
    request match {
      case req: FoodMaterialSellRequest =>
        (prevPartyType.isDefined
          && req.sender.ofType == prevPartyType.get
          && !foodRepo.capacityReached())
      case req: FoodMaterialBuyRequest => req.seller == this
      case req: PaymentRequest => req.payer == this
      case _ =>
        Utils.assertionFailed(s"Unknown request ${request.toString()}")
        false
    }

  def act(inbox: List[Message]): Unit = {
    currentTick += 1

    inbox.foreach { message =>
      message.content match {
        // node's own transaction was already processed
        case tx: Transaction[UtxoContent] =>
          if (message.sender != this && receiveTransaction(tx))
            handleTransaction(tx)
        case b: Block => val _ = receiveBlock(b)
        case req: ChannelRequest => handleChannelRequest(req)
        case _ => Utils.assertionFailed(s"Unknown message content: ${message.content.toString()}")
      }
    }

    val processedMaterials = foodRepo.getInState(FoodMaterialState.Processed)
    // TODO: do we want to send to all channnels?
    for {
      material <- processedMaterials
      channel <- channels
    } channel.makeRequest(new FoodMaterialSellRequest(channel, sender = this, material))

    mineBlock(time = currentTick)
  }

  def applyBehaviour(behaviour: PartyBehaviour): Unit = behaviour.apply(this)

  def getFoodMaterials(): List[FoodMaterial] = foodRepo.getAll()

  def recordOperation(operation: Operation[UtxoContent]): Unit = {
    Logger.log(s"new operation: ${operation.toString()}", this)
    val tx = operation match {
      case op: PaymentOperation =>
        new MoneyTransaction(
          op,
          initiator = this,
          time = currentTick,
        )
      case op: FoodOperation =>
        new FoodTransaction(op, initiator = this, time = currentTick)
      case op: TransferOperation =>
        new FoodTransaction(op, initiator = this, time = currentTick)
      case _ => throw new RuntimeException(s"Unknown operation: $operation")
    }

    // let the transaction owner record his transaction immediately,
    // other nodes will receive it in the next tick
    network.broadcast(tx, sender = this)
    val _ = receiveTransaction(tx)
  }

  protected def processMaterial(material: FoodMaterial, op: FoodOperation) = {
    foodRepo.updatePrice(material, op.pricingStrategy.apply(material))
    foodRepo.updateState(material, FoodMaterialState.Processed)
  }

  protected def transferFoodMaterials(materials: List[FoodMaterial], to: FoodChainParty): Unit = {
    Logger.log(s"Transfering ${materials.toString()} to ${to.id}", this)

    val transferOperation = new TransferOperation(materials, from = this, to = to)

    foodRepo.removeMany(materials)
    recordOperation(transferOperation)
  }

  protected def makePayment(amount: Double, to: FoodChainParty): Unit = {
    Logger.log(s"Paying ${Utils.formatPrice(amount)} to ${to.id}", this)
    if (amount > balance)
      Utils.assertionFailed(s"Not enough money to pay ${Utils.formatPrice(amount)} to ${to.id}")

    val paymentOperation = new PaymentOperation(amount, from = this, to = to)

    balance -= amount
    recordOperation(paymentOperation)
  }

  override def toString(): String = id
}
