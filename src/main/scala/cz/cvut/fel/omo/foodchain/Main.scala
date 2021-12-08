package cz.cvut.fel.omo
package foodchain

import ecosystem.EcosystemSimulation

object Main extends App {
  println("─" * 100)

  (new EcosystemSimulation).run()
  println("─" * 100)
}
