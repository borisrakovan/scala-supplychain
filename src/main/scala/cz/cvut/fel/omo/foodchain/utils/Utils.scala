package cz.cvut.fel.omo.foodchain.utils

import cz.cvut.fel.omo.foodchain.ecosystem.EcosystemConfig
import cz.cvut.fel.omo.foodchain.common.Logger

object Utils {
  def round2(n: Double): Double = n - (n % 0.01)
  def formatPrice(p: Double): String = f"$p%1.2f"

  def random(min: Double, max: Double): Double =
    min + (math.random() * (max - min))

  def assertionFailed(reason: String, forceError: Boolean = false): Unit =
    if (EcosystemConfig.Debug || forceError)
      throw new RuntimeException(reason)
    else
      Logger.error(reason)
}
