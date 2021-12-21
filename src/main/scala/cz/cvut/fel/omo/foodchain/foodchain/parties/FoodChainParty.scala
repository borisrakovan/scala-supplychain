package cz.cvut.fel.omo.foodchain.foodchain

import cz.cvut.fel.omo.foodchain.blockchain._
import cz.cvut.fel.omo.foodchain.common.{ Message, MessageQueue, Transferable }
import cz.cvut.fel.omo.foodchain.foodchain.channels._
import cz.cvut.fel.omo.foodchain.foodchain.transactions._
import cz.cvut.fel.utils.IdGenerator
import cz.cvut.fel.omo.foodchain.foodchain.operations._
import scala.collection.mutable.ListBuffer

object PartyIdGenerator extends IdGenerator {}

object FoodChainParty {
  val FoodChain: List[String] =
    List(
      // "farmer","importer", "regulator", "distributor", "supplier", "retailer", "customer",
      "farmer",
      "regulator",
      "distributor",
      "customer",
    )

  def getPreviousParty(forType: String): Option[String] = {
    if (!FoodChain.contains(forType))
      throw new RuntimeException(s"Unknown party type: $forType")
    if (forType != FoodChain(0))
      Some(FoodChain(FoodChain.indexOf(forType) - 1))
    else None
  }
}

abstract class FoodChainParty(
    val ofType: String,
    // type of the previous party in the chain
    val network: Network,
    val channels: List[Channel],
    initialMaterials: List[FoodMaterial],
    val initialBalance: Double,
    capacity: Int = Int.MaxValue,
  ) extends Node {
  val id = s"$ofType-${PartyIdGenerator.getNextId()}"
  val prevPartyType: Option[String] = FoodChainParty.getPreviousParty(forType = ofType)

  override val transactionValidationStrategy = new FoodChainValidationStrategy
  private val pricingStrategy: PricingStrategy = new DefaultPricingStrategy
  private var currentTick: Int = 0
  protected val foodRepo: FoodRepository = new InMemoryFoodRepository(initialMaterials, capacity)
  var balance: Double = initialBalance
  val expectedMaterials: ListBuffer[FoodMaterial] = ListBuffer.empty[FoodMaterial]

  def handleTransaction(tx: TX): Unit =
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
            material.price = pricingStrategy.apply(material)
            transferFoodMaterials(List(material), to = req.sender)
            log(s"Requesting payment of ${material.price.toString()} from ${req.sender.id}")
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
            throw new RuntimeException(
              s"can no longer satisfy incoming request ${request.toString()}"
            )
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
          throw new RuntimeException(s"Not enough money to pay ${req.amount}")
        else if (req.payer != this)
          throw new RuntimeException(s"Received payment request meant for someone else")
        else
          makePayment(req.amount, req.sender)

      case _ => throw new RuntimeException(s"Unknown request ${request.toString()}")
    }

  def offerChannelRequest(request: ChannelRequest): Boolean =
    request match {
      case req: FoodMaterialSellRequest =>
        (prevPartyType.isDefined
          && req.sender.ofType == prevPartyType.get
          && !foodRepo.capacityReached())
      case req: FoodMaterialBuyRequest => req.seller == this
      case req: PaymentRequest => req.payer == this
      case _ => throw new RuntimeException(s"Unknown channel request: ${request.toString()}")
    }

  def act(inbox: List[Message]): Unit = {
    currentTick += 1

    mineBlock()

    inbox.foreach { message =>
      message.content match {
        case tx: TX =>
          val _ = receiveTransaction(tx)
          handleTransaction(tx)

        case b: Block =>
          val _ = receiveBlock(b)

        case req: ChannelRequest =>
          handleChannelRequest(req)
        case _ =>
          throw new RuntimeException(s"Unknown message content: ${message.content.toString()}")
      }
    }

    val processedMaterials = foodRepo.getInState(FoodMaterialState.Processed)
    // TODO: do we want to send to all channnels?
    for {
      material <- processedMaterials
      channel <- channels
    } channel.makeRequest(new FoodMaterialSellRequest(channel, sender = this, material))
  }

  def getFoodMaterials(): List[FoodMaterial] = foodRepo.getAll()

  protected def recordOperation(operation: Operation[UtxoContent]) = {
    log(s"new operation: ${operation.toString()}")
    val tx = operation match {
      case op: PaymentOperation =>
        new MoneyTransaction[FoodChainParty](
          op,
          initiator = this,
        )
      case op: FoodOperation =>
        new FoodTransaction(op, initiator = this)

      case _ => throw new RuntimeException(s"Unknown operation: $operation")
    }

    // let the transaction owner record his transaction immediately,
    // other nodes will receive it in the next tick
    network.broadcast(tx, sender = this)
    // network.getParticipants().foreach { n =>
    // n.receiveTransaction(tx)
    // n.asInstanceOf[FoodChainParty].handleTransaction(tx)
    // }
    val txValid = receiveTransaction(tx)

  }

  protected def processMaterial(material: FoodMaterial) =
    foodRepo.updateState(material, FoodMaterialState.Processed)

  protected def transferFoodMaterials(materials: List[FoodMaterial], to: FoodChainParty): Unit = {
    log(s"Transferring ${materials.toString()} to ${to.id}")

    val transferOperation = new TransferOperation(materials, from = this, to = to)

    foodRepo.removeMany(materials)
    recordOperation(transferOperation)
  }

  protected def makePayment(amount: Double, to: FoodChainParty): Unit = {
    log(s"Paying ${amount.toString()} to ${to.id}")
    if (amount > balance)
      // TODO
      throw new RuntimeException(s"Not enough money to pay ${amount.toString()} to ${to.id}")

    val paymentOperation = new PaymentOperation(amount, from = this, to = to)

    balance -= amount
    recordOperation(paymentOperation)
  }

  override def toString(): String = id
}
