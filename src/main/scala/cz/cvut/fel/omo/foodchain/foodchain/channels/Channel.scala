package cz.cvut.fel.omo.foodchain.foodchain.channels

import scala.collection.mutable
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import scala.util.Random
import cz.cvut.fel.omo.foodchain.foodchain.{ MessageQueue, Message }
import cz.cvut.fel.omo.foodchain.blockchain.Node
import cz.cvut.fel.omo.foodchain.Logger
import cz.cvut.fel.omo.foodchain.utils.Utils

class Channel(val name: String) extends MessageQueue {
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var participants: List[FoodChainParty] = List.empty[FoodChainParty]

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val requestQueue = mutable.Queue[(ChannelRequest, FoodChainParty)]()

  // TODO: resend these at some point in a cycle,
  // or at least several times per simulation
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val pendingRequests = mutable.Queue[ChannelRequest]()

  def collectMessages(): Map[Node, List[Message]] = {
    val messages = requestQueue.toList
    requestQueue.clear()
    participants.map { p =>
      (p -> messages
        .filter { case (_, rec) => rec == p }
        .map {
          case (req, _) =>
            new Message(sender = req.sender, recipient = p, content = req)
        })
    }.toMap
  }

  def makeRequest(request: ChannelRequest): Unit = {
    if (!participants.contains(request.sender))
      Utils.assertionFailed("Sender is not a participant in this channel")
    // always suffle the participants in the chain to make sure
    // that everybody has equal chance to respond to the request
    val acceptedBy = Random
      .shuffle(participants)
      .find(p => p.offerChannelRequest(request))

    acceptedBy match {
      case Some(rec) => val _ = requestQueue.enqueue((request, rec))
      case None => val _ = pendingRequests.enqueue(request)
    }
    if (acceptedBy.isDefined)
      Logger.info(s"request: ${request.toString()} accepted by ${acceptedBy.get.id}")
  }

  def registerParty(party: FoodChainParty): Unit =
    participants = participants :+ party

  def removeParty(party: FoodChainParty): Unit =
    participants = participants.filterNot(_ == party)
}
