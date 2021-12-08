package cz.cvut.fel.omo.foodchain.blockchain

object Node {
  val BlockSize: Int = 1
}

trait Node {
  val id: String
  val transactionValidationStrategy: TransactionValidationStrategy
  // a list of unspent transactions in the system, the only necessary minimal state of a node
  // var utxos: List[Utxo[UtxoContent]] = initialState()
  var blockChain: BlockChain[Node, Operation[UtxoContent]] =
    new BlockChain[Node, Operation[UtxoContent]](List.empty[Block])

  type TX = Transaction[Node, UtxoContent, Operation[UtxoContent]]
  // stores unconfirmed transactions
  var transactionPool: List[TX] = List.empty[TX]

  private var utxos: Option[List[Utxo[UtxoContent]]] = None

  // lazy initialization necessary due to mutual reference
  def initializeUtxos(initialUtxos: List[Utxo[UtxoContent]]): Unit = utxos = Some(initialUtxos)
  def getUtxos(): List[Utxo[UtxoContent]] = utxos match {
    case Some(value) => value
    case None => throw new RuntimeException(s"Utxos were not initialized for node $id")
  }
  protected def getOwnedUtxos(): List[Utxo[UtxoContent]] = getUtxos().filter(_.owner == this)

  def broadcastBlock(block: Block): Unit
  def broadcastTransaction(tx: TX): Unit

  def sign(utxo: Utxo[UtxoContent]): String = s"signature_$id"

  def receiveTransaction(tx: Transaction[Node, UtxoContent, Operation[UtxoContent]]): Boolean = {
    val updatedTransactionPool = transactionPool :+ tx

    val txValid = updatedTransactionPool
      .foldLeft[Option[List[Utxo[UtxoContent]]]](Some(getUtxos()))(applyTransaction) match {
      case Some(_) =>
        log("received new transaction")
        true
      case None =>
        log("received invalid transaction")
        false
    }

    if (txValid) {
      transactionPool = updatedTransactionPool
      if (transactionPool.size == Node.BlockSize)
        if (id == "farmer") {
          // TODO: implement proof of work
          // for now, the system acts as if the block was always immediately mined by a node with id "farmer"
          val newBlock = new Block(blockChain.history.lastOption, transactionPool, 1)
          log("mined block")
          broadcastBlock(newBlock)
          transactionPool = List.empty[TX]
        }
    }
    txValid
  }

  def receiveBlock(block: Block): Boolean =
    /** Check if the previous block referenced by the block exists and is valid.
      *   Check that the timestamp of the block is greater than that of the previous blockfn. 2 and less than 2 hours into the future
      *   Check that the proof of work on the block is valid.
      *   Let S[0] be the state at the end of the previous block.
      *   Suppose TX is the block's transaction list with n transactions.
      *   For all i in 0...n-1, set S[i+1] = APPLY(S[i],TX[i]) If any application returns an error, exit and return false.
      *   Return true, and register S[n] as the state at the end of this block.
      */ {
      // log(s"received new block. current utxos: $utxos")
      log(s"received new block")
      blockChain.append(block) match {
        case Some(updatedBlockChain) =>
          block
            .transactions
            .foldLeft[Option[List[Utxo[UtxoContent]]]](Some(getUtxos()))(applyTransaction) match {
            case Some(updatedUtxos) =>
              // log(s"updatedUtxos: $updatedUtxos")
              updateState(updatedUtxos, updatedBlockChain, List.empty[TX])
              true
            case None =>
              log(
                s"Can't append block ${block.toString()} to blockchain. Some of the transasctions are invalid."
              )
              false
          }

        case None =>
          log(
            s"Can't append block ${block.toString()} to blockchain. The block is not a valid continuation."
          )
          false
      }
    }

  protected def updateState(
      updatedUtxos: List[Utxo[UtxoContent]],
      updatedBlockChain: BlockChain[Node, Operation[UtxoContent]],
      updatedTransactionPool: List[TX],
    ): Unit = {
    // log(s"updatedUtxos: $updatedUtxos")
    // log("updating state")
    utxos = Some(updatedUtxos)
    blockChain = updatedBlockChain
    transactionPool = updatedTransactionPool
  }

  private def validateTransaction(
      tx: TX,
      utxos: List[Utxo[UtxoContent]],
    ): Boolean = {
    // step 1: check that all of the referenced inputs are indeed in the current state
    val inputsValid = tx
      .inputs
      .find { txInput =>
        !utxos.contains(txInput.utxo) || !txInput.utxo.validateSignature(txInput.signature)
      }
      .isEmpty

    // step 2: check that the tx outputs can actually be derived from the tx inputs according
    // to some client-defined validation strategy
    lazy val outputsValid = transactionValidationStrategy.validate(tx)
    inputsValid && outputsValid
  }

  private def applyTransaction(
      utxosOption: Option[List[Utxo[UtxoContent]]],
      tx: TX,
    ): Option[List[Utxo[UtxoContent]]] =
    /*
      For each input in TX:
      If the referenced UTXO is not in S, return an error.
      If the provided signature does not match the owner of the UTXO, return an error.
      If the sum of the denominations of all input UTXO is less than the sum of the denominations of all output UTXO, return an error.
      Return S with all input UTXO removed and all output UTXO added.
     */
    utxosOption.flatMap { utxos =>
      if (validateTransaction(tx, utxos))
        Some(
          utxos.filterNot(utxo => tx.inputs.map(_.utxo).contains(utxo))
            ++ tx.outputs
        )
      else
        None
    }

  protected def log(str: String): Unit = println(s"[$id] $str")
}
