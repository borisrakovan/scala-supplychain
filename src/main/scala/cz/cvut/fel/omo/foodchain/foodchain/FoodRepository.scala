package cz.cvut.fel.omo.foodchain.foodchain

import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterialState
import scala.collection.mutable.{ ListBuffer, Map }

trait FoodRepository {
  val capacity: Int
  def addMany(materials: List[FoodMaterial]): Unit
  def removeMany(materials: List[FoodMaterial]): Unit
  def find(material: FoodMaterial): Option[FoodMaterial]
  def getAll(): List[FoodMaterial]
  def getInState(state: FoodMaterialState): List[FoodMaterial]
  def updateState(material: FoodMaterial, newState: FoodMaterialState)
  def capacityReached(): Boolean = getAll().length >= capacity
}

class InMemoryFoodRepository(val initialMaterials: List[FoodMaterial], val capacity: Int)
    extends FoodRepository {
  // TODO: move price here as well?
  private val foodMaterials: ListBuffer[FoodMaterial] =
    ListBuffer.empty[FoodMaterial] ++ initialMaterials
  private val materialStates: Map[FoodMaterial, FoodMaterialState] =
    Map(
      initialMaterials
        .map(m => (m, FoodMaterialState.Waiting)): _*
    )
  def addMany(materials: List[FoodMaterial]): Unit = {
    foodMaterials ++= materials
    materials.foreach(m => materialStates(m) = FoodMaterialState.Waiting)
  }

  def removeMany(materials: List[FoodMaterial]): Unit = {
    foodMaterials --= materials
    materialStates --= materials
  }

  def find(material: FoodMaterial): Option[FoodMaterial] = foodMaterials.find(_ == material)

  def getAll(): List[FoodMaterial] = foodMaterials.toList

  def getInState(state: FoodMaterialState): List[FoodMaterial] =
    materialStates.collect {
      case (k, v) if v == state => k
    }.toList

  def updateState(material: FoodMaterial, newState: FoodMaterialState) =
    find(material) match {
      case Some(m) => materialStates.put(material, newState)
      case None => throw new RuntimeException(s"Material ${material.id} not in repo.")
    }
}
