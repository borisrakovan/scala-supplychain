package cz.cvut.fel.omo.foodchain.foodchain.transactions

import cz.cvut.fel.omo.foodchain.blockchain.Operation
import cz.cvut.fel.omo.foodchain.blockchain.Transaction
import cz.cvut.fel.omo.foodchain.foodchain.Money
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty

class MoneyTransaction(
    operation: Operation[Money],
    initiator: FoodChainParty,
    time: Long,
  ) extends Transaction[Money](operation, initiator, time)
