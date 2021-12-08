package cz.cvut.fel.omo.foodchain.foodchain.transactions
import cz.cvut.fel.omo.foodchain.blockchain.Operation
import cz.cvut.fel.omo.foodchain.blockchain.Node
import cz.cvut.fel.omo.foodchain.blockchain.Transaction
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial

class FoodTransaction[+N <: Node](
    operation: Operation[FoodMaterial],
    initiator: N,
  ) extends Transaction[N, FoodMaterial, Operation[FoodMaterial]](operation, initiator)
