package cz.cvut.fel.omo.foodchain.foodchain

import scala.math.random
import cz.cvut.fel.omo.foodchain.common.Logger
import cz.cvut.fel.omo.foodchain.foodchain.transactions.MoneyTransaction
import cz.cvut.fel.omo.foodchain.blockchain.BlockChain

trait PartyBehaviour {
  def apply(party: FoodChainParty): Unit
}

class HostileBehaviour(
    val doubleSpendProba: Double,
    val blockMutationProba: Double,
  ) extends PartyBehaviour {
  def apply(party: FoodChainParty): Unit = {

    if (random() <= doubleSpendProba)
      applyDoubleSpend(party)

    if (random() <= blockMutationProba)
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

  private def applyMutation(party: FoodChainParty): Unit =
    // try removing some transaction from the block
    party.blockChain.history.lastOption match {
      case Some(block) => party.blockChain = new BlockChain(party.blockChain.history.dropRight(1))
      case None =>
    }
}
