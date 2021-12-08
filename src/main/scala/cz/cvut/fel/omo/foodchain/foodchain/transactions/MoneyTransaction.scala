package cz.cvut.fel.omo.foodchain.foodchain.transactions

import cz.cvut.fel.omo.foodchain.blockchain.Operation
import cz.cvut.fel.omo.foodchain.blockchain.Node
import cz.cvut.fel.omo.foodchain.blockchain.Transaction
import cz.cvut.fel.omo.foodchain.foodchain.Money

class MoneyTransaction[+N <: Node](
    operation: Operation[Money],
    initiator: N,
  ) extends Transaction[N, Money, Operation[Money]](operation, initiator)
