package cz.cvut.fel.omo.foodchain.ecosystem

import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import cz.cvut.fel.omo.foodchain.foodchain.channels.Channel
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.foodchain.EcosystemNetwork
import cz.cvut.fel.omo.foodchain.utils.Utils

object Ecosystem {
  val FoodChain: List[String] =
    List(
      "farmer",
      "importer",
      "regulator",
      "distributor",
      "retailer",
      "customer",
    )

  def getPreviousParty(forType: String): Option[String] = {
    if (!FoodChain.contains(forType))
      Utils.assertionFailed(s"Unknown party type: $forType", forceError = true)
    if (forType != FoodChain(0))
      Some(FoodChain(FoodChain.indexOf(forType) - 1))
    else None
  }
}

class Ecosystem(
    val network: EcosystemNetwork,
    val parties: List[FoodChainParty],
    val channels: List[Channel],
    val foodMaterials: List[FoodMaterial],
  ) {
  def getTrustedParty(): FoodChainParty = {
    // as in common blockchain setting, take the chain that is shared by the majority of nodes
    val trustedParties = parties.groupBy(_.blockChain.getLastHash()).maxBy(_._2.size)._2
    trustedParties(0)
  }

  def getHostileParty(): Option[FoodChainParty] =
    EcosystemConfig.HostileNode match {
      case Some(pid) => parties.find(_.id.startsWith(pid))
      case None => None
    }
}
