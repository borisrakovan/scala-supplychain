package cz.cvut.fel.omo
package foodchain

import ecosystem.EcosystemSimulation
import cz.cvut.fel.omo.foodchain.ecosystem.ConcreteEcosystemFactory
import cz.cvut.fel.omo.foodchain.ecosystem.EcosystemFactory
import cz.cvut.fel.omo.foodchain.ecosystem.Ecosystem

object Main extends App {
  println("─" * 100)

  val fac: EcosystemFactory = new ConcreteEcosystemFactory
  val ecosystem: Ecosystem = fac.create()

  (new EcosystemSimulation(ecosystem)).run()

  println("─" * 100)
}
