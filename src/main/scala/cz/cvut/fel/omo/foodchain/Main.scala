package cz.cvut.fel.omo
package foodchain

import ecosystem.EcosystemSimulation
import cz.cvut.fel.omo.foodchain.ecosystem.ConcreteEcosystemFactory
import cz.cvut.fel.omo.foodchain.ecosystem.EcosystemFactory
import cz.cvut.fel.omo.foodchain.ecosystem.Ecosystem
import cz.cvut.fel.omo.foodchain.ui.CommandLineInterface
import cz.cvut.fel.omo.foodchain.ui.UserInterface

object Main extends App {
  val fac: EcosystemFactory = new ConcreteEcosystemFactory
  val ecosystem: Ecosystem = fac.create()
  val userInterface: UserInterface = new CommandLineInterface

  (new EcosystemSimulation(ecosystem, userInterface)).run()
}
