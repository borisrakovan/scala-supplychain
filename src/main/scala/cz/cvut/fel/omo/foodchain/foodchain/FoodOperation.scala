package cz.cvut.fel.omo.foodchain.foodchain

import cz.cvut.fel.omo.foodchain.blockchain.Operation

import cz.cvut.fel.omo.foodchain.blockchain.Utxo

abstract class FoodOperation extends Operation[FoodMaterial]

class TransferOperation(
    val materials: List[FoodMaterial],
    val from: FoodChainParty,
    val to: FoodChainParty,
  ) extends FoodOperation {
  override def getInputs(): List[Utxo[FoodMaterial]] = materials.map(new Utxo(from, _))
  override def getOutputs(): List[Utxo[FoodMaterial]] = materials.map(new Utxo(to, _))
}

class PaymentOperation(
    val inputs: List[Utxo[Money]],
    val outputs: List[Utxo[Money]],
    val from: FoodChainParty,
    val to: FoodChainParty,
  ) extends Operation[Money] {
  override def getInputs(): List[Utxo[Money]] = inputs
  override def getOutputs(): List[Utxo[Money]] = outputs

  def getAmount(): Double = outputs.filter(_.owner == to).map(_.content.amount).sum
}

abstract class SimpleFoodOperation(
    val material: FoodMaterial,
    val party: FoodChainParty,
  ) extends FoodOperation {
  override def getInputs(): List[Utxo[FoodMaterial]] = List(new Utxo(party, material))
  override def getOutputs(): List[Utxo[FoodMaterial]] = List(new Utxo(party, material))
}

class StorageOperation(
    material: FoodMaterial,
    party: FoodChainParty,
    val length: Int,
    val temperature: Int,
    val humidity: Int,
  ) extends SimpleFoodOperation(material, party) {}
