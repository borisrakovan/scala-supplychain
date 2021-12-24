package cz.cvut.fel.omo.foodchain.blockchain

import cz.cvut.fel.omo.foodchain.blockchain.security._
import cz.cvut.fel.omo.foodchain.common.Logger
import cz.cvut.fel.omo.foodchain.common.LogSource
import cz.cvut.fel.omo.foodchain.blockchain.consensus.ProofOfWork
import scala.util.Success
import scala.util.Failure
import cz.cvut.fel.omo.foodchain.utils.Utils
import cz.cvut.fel.omo.foodchain.ecosystem.EcosystemConfig

object Node {
  var mined = 0
}
trait Node extends LogSource {
  val id: String
  val transactionValidationStrategy: TransactionValidationStrategy
  val network: Network

  val securityLog: SecurityLog = new InMemorySecurityLog
  // a list of unspent transactions in the system, the only necessary minimal state of a node
  var blockChain: BlockChain =
    new BlockChain(List.empty[Block])

  // stores unconfirmed transactions
  var transactionPool: List[Transaction[UtxoContent]] = List.empty[Transaction[UtxoContent]]

  private var utxos: List[Utxo[UtxoContent]] = null
  private var genesisState: List[Utxo[UtxoContent]] = null

  private val pow = new ProofOfWork(network)

  // lazy initialization necessary due to mutual reference
  def initializeState(genesisState: List[Utxo[UtxoContent]]): Unit = {
    utxos = genesisState
    this.genesisState = genesisState
  }

  def getUtxos(): List[Utxo[UtxoContent]] = {
    if (utxos == null)
      Utils.assertionFailed(s"Utxos were not initialized for node $id", forceError = true)
    utxos
  }

  def getGenesisState(): List[Utxo[UtxoContent]] = {
    if (genesisState == null)
      Utils.assertionFailed(s"Utxos were not initialized for node $id", forceError = true)
    genesisState
  }

  protected def getLiveUtxos(
    ): List[Utxo[UtxoContent]] = applyTransactionsOnState(transactionPool) match {
    case Right(updatedUtxos) => updatedUtxos
    case Left(violation) => violation.raiseException()
  }

  def getLiveOwnedUtxos(): List[Utxo[UtxoContent]] =
    getLiveUtxos().filter(_.owner == this)

  def sign(utxo: Utxo[UtxoContent]): String = s"signature_$id"

  def mineBlock(time: Long): Unit =
    if (transactionPool.size >= EcosystemConfig.BlockSize) {
      val transactions = transactionPool.take(EcosystemConfig.BlockSize)

      val lastHash = blockChain.getLastHash()
      val result = pow.attempt(lastHash, transactions)

      result match {
        case Some((hash, nonce)) =>
          val block = new Block(lastHash, transactions, nonce, hash, time)
          Logger.error(s"Mined block ${block.toString()}!", this)

          network.broadcast(block, this)
          Node.mined += 1
          Logger.error(Node.mined.toString())
        // val _ = receiveBlock(block)

        case None =>
      }
    }

  def receiveTransaction(tx: Transaction[UtxoContent]): Boolean = {
    val updatedTransactionPool = transactionPool :+ tx

    applyTransactionsOnState(updatedTransactionPool) match {
      case Right(_) =>
        // Logger.log(s"received valid transaction: ${tx.toString()} ${tx.operation.toString()}", this)
        transactionPool = updatedTransactionPool
        true
      case Left(violation) =>
        if (tx.initiator != this) {
          Logger.warn(s"Detected ${violation.toString()}", this)
          securityLog.report(violation)
        }
        // throw new RuntimeException(
        //   s"${id}: received invalid transaction: ${tx.toString()} ${tx.operation.toString()}"
        // )
        false
    }
  }

  def receiveBlock(block: Block): Boolean =
    /** Check if the previous block referenced by the block exists and is valid.
      *   Check that the timestamp of the block is greater than that of the previous blockfn. 2 and less than 2 hours into the future
      *   Check that the proof of work on the block is valid.
      *   Let S[0] be the state at the end of the previous block.
      *   Suppose TX is the block's transaction list with n transactions.
      *   For all i in 0...n-1, set S[i+1] = APPLY(S[i],TX[i]) If any application returns an error, exit and return false.
      *   Return true, and register S[n] as the state at the end of this block.
      */
    blockChain.append(block) match {
      case Success(updatedBlockChain) =>
        applyTransactionsOnState(block.transactions) match {
          case Right(updatedUtxos) =>
            // log(s"updatedUtxos: $updatedUtxos")
            val updatedTxPool =
              transactionPool.filterNot(tx => updatedBlockChain.flatTransactions().contains(tx))
            updateState(updatedUtxos, updatedBlockChain, updatedTxPool)
            true
          case Left(violation) =>
            Logger.log(
              s"Can't append block ${block.toString()} to blockchain. Some of the transasctions are invalid.",
              this,
            )
            violation.raiseException()
          // false
        }

      case Failure(exception) =>
        exception match {
          case e: AlreadyMinedException =>
            Logger.warn(e.getMessage(), this)
            true
          case _ => throw exception
        }
    }

  protected def updateState(
      updatedUtxos: List[Utxo[UtxoContent]],
      updatedBlockChain: BlockChain,
      updatedTransactionPool: List[Transaction[UtxoContent]],
    ): Unit = {
    // log(s"updatedUtxos: $updatedUtxos")
    // Logger.log("updating state", this)
    utxos = updatedUtxos
    blockChain = updatedBlockChain
    transactionPool = updatedTransactionPool
  }

  private def applyTransactionsOnState(
      transactions: List[Transaction[UtxoContent]]
    ): Either[BlockChainViolation, List[Utxo[UtxoContent]]] = {
    val initialState = getUtxos()

    type ResultType = Either[BlockChainViolation, List[Utxo[UtxoContent]]]
    val finalState = transactions.foldLeft[ResultType](Right(initialState))(applyTransaction)

    finalState
  }

  private def applyTransaction(
      utxosM: Either[BlockChainViolation, List[Utxo[UtxoContent]]],
      tx: Transaction[UtxoContent],
    ): Either[BlockChainViolation, List[Utxo[UtxoContent]]] =
    /*
      For each input in TX:
      If the referenced UTXO is not in S, return an error.
      If the provided signature does not match the owner of the UTXO, return an error.
      If the sum of the denominations of all input UTXO is less than the sum of the denominations of all output UTXO, return an error.
      Return S with all input UTXO removed and all output UTXO added.
     */

    utxosM match {
      case Left(v) => Left(v)
      case Right(utxos) =>
        validateTransaction(tx, utxos) match {
          case Left(v) => Left(v)
          case Right(_) =>
            val filtered = utxos.filterNot(utxo => tx.inputs.map(_.utxo).contains(utxo))
            Right(filtered ++ tx.outputs)
        }
    }

  private def validateTransaction(
      tx: Transaction[UtxoContent],
      utxos: List[Utxo[UtxoContent]],
    ): Either[BlockChainViolation, Transaction[UtxoContent]] = {

    @scala.annotation.tailrec
    def validateInputs(txInputs: List[TransactionInput[UtxoContent]]): Option[BlockChainViolation] =
      txInputs match {
        case inp :: rest =>
          val inputValid = utxos.contains(inp.utxo)
          lazy val sigValid = inp.utxo.validateSignature(inp.signature)
          if (!inputValid)
            Some(new DoubleSpendingViolation(tx.initiator, tx.time, tx, inp))
          else if (!sigValid)
            Some(new SignatureForgeryViolation(tx.initiator, tx.time, tx, inp.signature))
          else
            validateInputs(rest)
        case Nil =>
          None
      }

    // step 1: check that all of the referenced inputs are indeed in the current state
    validateInputs(tx.inputs) match {
      case Some(violation) => Left(violation)
      case None =>
        // step 2: check that the tx outputs can actually be derived from the tx inputs according
        // to some client-defined validation strategy
        if (transactionValidationStrategy.validate(tx))
          Right(tx)
        else
          Left(new InvalidTransactionViolation(tx.initiator, tx.time, tx))
    }

  }
}
