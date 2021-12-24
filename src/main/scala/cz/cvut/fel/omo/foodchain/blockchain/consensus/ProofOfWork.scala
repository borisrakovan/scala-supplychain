package cz.cvut.fel.omo.foodchain.blockchain.consensus

import cz.cvut.fel.omo.foodchain.utils.Crypto
import cz.cvut.fel.omo.foodchain.blockchain.Transaction
import cz.cvut.fel.omo.foodchain.blockchain.UtxoContent
import cz.cvut.fel.omo.foodchain.ecosystem.EcosystemConfig
import cz.cvut.fel.omo.foodchain.blockchain.Network
import scala.util.Random

object ProofOfWork {
  val Difficulty: Int = 2
}

class ProofOfWork(val network: Network) {
  private val random: Random = new Random

  def attempt(
      lastHash: String,
      transactions: List[Transaction[UtxoContent]],
    ): Option[(String, Long)] = {

    // the probability of a mined block in a period should be 1 / AveragemineSuccessPeriod
    // there are numParticipants * numMineAttempts per period and the proba of successfull attempt
    // is 1 / 16^Difficulty
    val numParticipants = network.getParticipants().size
    val numMineAttempts =
      (scala
        .math
        .pow(
          16,
          ProofOfWork.Difficulty,
        ) / (EcosystemConfig.AverageMineSuccessPeriod * numParticipants)).toInt

    @scala.annotation.tailrec
    def powHelper(i: Int): Option[(String, Long)] =
      if (i == numMineAttempts)
        None
      else {
        val nonce = random.nextInt()
        validateProof(lastHash, transactions, proof = nonce) match {
          case Some(hash) => Some((hash, nonce))
          case None => powHelper(i + 1)
        }
      }

    powHelper(0)
  }

  def validateProof(
      lastHash: String,
      transactions: List[Transaction[UtxoContent]],
      proof: Long,
    ): Option[String] = {
    val descriptor = makeDescriptor(lastHash, transactions, proof)
    val guess = Crypto.sha256Hash(descriptor)

    if ((guess take ProofOfWork.Difficulty) == "0".repeat(ProofOfWork.Difficulty))
      Some(guess)
    else
      None
  }

  private def makeDescriptor(
      lastHast: String,
      transactions: List[Transaction[UtxoContent]],
      proof: Long,
    ): String = {
    val txHash = transactions.map(_.hashCode()).mkString(":")
    s"${lastHast}:${txHash}:${proof.toString()}"
  }
}
