package cz.cvut.fel.omo.foodchain.foodchain.transactions
import cz.cvut.fel.omo.foodchain.blockchain.Operation
import cz.cvut.fel.omo.foodchain.blockchain.Transaction
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty

class FoodTransaction(
    operation: Operation[FoodMaterial],
    initiator: FoodChainParty,
    time: Long,
  ) extends Transaction[FoodMaterial](operation, initiator, time)
