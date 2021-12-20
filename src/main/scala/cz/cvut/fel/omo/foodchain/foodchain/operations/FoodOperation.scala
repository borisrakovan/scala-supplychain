package cz.cvut.fel.omo.foodchain.foodchain.operations

import cz.cvut.fel.omo.foodchain.blockchain.Operation

import cz.cvut.fel.omo.foodchain.blockchain.Utxo
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty

abstract class FoodOperation extends Operation[FoodMaterial]

class TransferOperation(
    val materials: List[FoodMaterial],
    val from: FoodChainParty,
    val to: FoodChainParty,
  ) extends FoodOperation {
  override def getInputs(): List[Utxo[FoodMaterial]] = materials.map(new Utxo(from, _))
  override def getOutputs(): List[Utxo[FoodMaterial]] = materials.map(new Utxo(to, _))

  override def toString(): String =
    s"${super.toString()}(${from.toString()} -> ${to.toString()}) (${materials.map(_.toString()).mkString(", ")})"
}

abstract class SimpleFoodOperation(
    val materials: List[FoodMaterial],
    val party: FoodChainParty,
  ) extends FoodOperation {
  override def getInputs(): List[Utxo[FoodMaterial]] = materials.map(new Utxo(party, _))
  override def getOutputs(): List[Utxo[FoodMaterial]] = materials.map(new Utxo(party, _))
}

class StorageOperation(
    material: FoodMaterial,
    party: FoodChainParty,
    val length: Int,
    val temperature: Int,
    val humidity: Int,
  ) extends SimpleFoodOperation(List(material), party) {}

class DistributionOperation(
    materials: List[FoodMaterial],
    party: FoodChainParty,
    val distance: Int,
  ) extends SimpleFoodOperation(materials, party) {}

class HarvestOperation(
    materials: List[FoodMaterial],
    party: FoodChainParty,
  ) extends SimpleFoodOperation(materials, party) {}

class InspectionOperation(
    material: FoodMaterial,
    party: FoodChainParty,
  ) extends SimpleFoodOperation(List(material), party) {}
