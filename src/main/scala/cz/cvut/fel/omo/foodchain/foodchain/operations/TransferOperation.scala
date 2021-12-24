package cz.cvut.fel.omo.foodchain.foodchain.operations

import cz.cvut.fel.omo.foodchain.blockchain.Operation
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import cz.cvut.fel.omo.foodchain.blockchain.Utxo

class TransferOperation(
    val materials: List[FoodMaterial],
    val from: FoodChainParty,
    val to: FoodChainParty,
  ) extends Operation[FoodMaterial] {
  override def getInputs(): List[Utxo[FoodMaterial]] = materials.map(new Utxo(from, _))
  override def getOutputs(): List[Utxo[FoodMaterial]] = materials.map(new Utxo(to, _))

  override def toString(): String =
    s"Transfer(from=${from.toString()}, to=${to.toString()})"
}
