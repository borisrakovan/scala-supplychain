package cz.cvut.fel.omo.foodchain.foodchain.operations

import cz.cvut.fel.omo.foodchain.blockchain.Operation

import cz.cvut.fel.omo.foodchain.blockchain.Utxo
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import cz.cvut.fel.omo.foodchain.foodchain.PricingStrategy
import cz.cvut.fel.omo.foodchain.foodchain.DefaultPricingStrategy
import cz.cvut.fel.omo.foodchain.foodchain.AbsolutePricingStrategy
import scala.util.Random
import cz.cvut.fel.omo.foodchain.foodchain.RelativePricingStrategy

abstract class FoodOperation(
    val materials: List[FoodMaterial],
    val party: FoodChainParty,
    val parameters: Map[String, Any],
    val pricingStrategy: PricingStrategy,
  ) extends Operation[FoodMaterial]() {
  override def getInputs(): List[Utxo[FoodMaterial]] = materials.map(new Utxo(party, _))
  override def getOutputs(): List[Utxo[FoodMaterial]] = materials.map(new Utxo(party, _))

  override def toString(): String =
    s"${super.toString()}(${parameters.map(p => s"${p._1}=${p._2}").mkString(", ")})"
}

class StorageOperation(
    material: FoodMaterial,
    party: FoodChainParty,
    val length: Int,
    val temperature: Int,
    val humidity: Int,
  ) extends FoodOperation(
      List(material),
      party,
      Map(
        "length" -> length,
        "temperature" -> temperature,
        "humidity" -> humidity,
      ),
      new AbsolutePricingStrategy(delta = Random.between(10, 50)),
    ) {}

class DistributionOperation(
    materials: List[FoodMaterial],
    party: FoodChainParty,
    val distance: Int,
  ) extends FoodOperation(
      materials,
      party,
      Map("distance" -> distance),
      new AbsolutePricingStrategy(delta = Random.between(20, 40)),
    ) {}

class HarvestOperation(
    materials: List[FoodMaterial],
    party: FoodChainParty,
  ) extends FoodOperation(
      materials,
      party,
      Map.empty,
      new RelativePricingStrategy(multiplier = Random.between(1.1, 1.2)),
    ) {}

class InspectionOperation(
    material: FoodMaterial,
    party: FoodChainParty,
  ) extends FoodOperation(
      List(material),
      party,
      Map.empty,
      new DefaultPricingStrategy,
    ) {}

class MarketingOperation(
    materials: List[FoodMaterial],
    party: FoodChainParty,
    discountPercentage: Option[Double],
  ) extends FoodOperation(
      materials,
      party,
      Map("discountPercentage" -> discountPercentage.getOrElse(0.0)),
      new RelativePricingStrategy(multiplier = (100 - discountPercentage.getOrElse(0d)) / 100),
    ) {}

class ImportOperation(
    materials: List[FoodMaterial],
    party: FoodChainParty,
  ) extends FoodOperation(
      materials,
      party,
      Map.empty,
      new RelativePricingStrategy(multiplier = 1.1),
    ) {}
