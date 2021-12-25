package cz.cvut.fel.omo.foodchain.foodchain

import scala.math.random
import cz.cvut.fel.omo.foodchain.Logger
import cz.cvut.fel.omo.foodchain.foodchain.transactions.MoneyTransaction
import cz.cvut.fel.omo.foodchain.blockchain.BlockChain
import cz.cvut.fel.omo.foodchain.Config
import cz.cvut.fel.omo.foodchain.blockchain.Block

trait PartyBehaviour {
  def apply(party: FoodChainParty): Unit
}

class HostileBehaviour extends PartyBehaviour {
  private val doubleSpendProba =
    Config.DoubleSpendPeriodProbability

  private val blockMutationProba = Config.BlockMutationProbability
  def apply(party: FoodChainParty): Unit = {

    if (random() <= doubleSpendProba)
      applyDoubleSpend(party)

    if (
        party.currentTick == Config.NumSimulationSteps
        && random() <= blockMutationProba
    )
      applyMutation(party)
  }

  private def applyDoubleSpend(party: FoodChainParty): Unit = {
    val prevPayments = party
      .blockChain
      .flatTransactions()
      .collect {
        case tx: MoneyTransaction if tx.initiator == party =>
          tx.operation
      }

    prevPayments.lastOption match {
      case Some(payment) =>
        // try double spending some already spent utxos
        Logger.warn(s"Trying to double spend ${payment.toString()}", party)
        party.recordOperation(payment)
      case None =>
    }
  }

  private def applyMutation(party: FoodChainParty): Unit = {
    // try removing some transaction from the block
    Logger.warn(s"Trying to mutate the blockchain", party)
    party.blockChain.history.lastOption match {
      case Some(_) =>
        val oldBlock = party.blockChain.history.last
        val newBlock = new Block(
          prevBlockHash = oldBlock.prevBlockHash,
          nonce = oldBlock.nonce,
          hash = oldBlock.hash,
          time = oldBlock.time,
          transactions = oldBlock.transactions.dropRight(1),
        )

        party.blockChain = new BlockChain(party.blockChain.history.dropRight(1) :+ newBlock)

      case None =>
    }
  }
}
