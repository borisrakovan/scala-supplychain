package cz.cvut.fel.omo.foodchain.ecosystem

import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty
import cz.cvut.fel.omo.foodchain.foodchain.channels.Channel
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.foodchain.EcosystemNetwork
import cz.cvut.fel.omo.foodchain.blockchain.BlockChain
import cz.cvut.fel.omo.foodchain.blockchain.Operation
import cz.cvut.fel.omo.foodchain.blockchain.UtxoContent
import cz.cvut.fel.omo.foodchain.blockchain.Node

class Ecosystem(
    val network: EcosystemNetwork,
    val parties: List[FoodChainParty],
    val channels: List[Channel],
    val foodMaterials: List[FoodMaterial],
  ) {
  def getTrustedBlockChain(): BlockChain[Node, Operation[UtxoContent]] =
    // TODO
    // val maxConfirmedLength = parties.map(_.blockChain.history.last.)
    parties(0).blockChain
}
