package cz.cvut.fel.omo.foodchain.foodchain

import cz.cvut.fel.omo.foodchain.utils.Utils

trait PricingStrategy {
  def apply(product: Tradeable): Double
}

class DefaultPricingStrategy extends PricingStrategy {
  def apply(product: Tradeable): Double = product.getPrice()
}

class RelativePricingStrategy(val multiplier: Double) extends PricingStrategy {
  def apply(product: Tradeable): Double = Utils.round2(product.getPrice() * multiplier)
}

class AbsolutePricingStrategy(val delta: Double) extends PricingStrategy {
  def apply(product: Tradeable): Double = Utils.round2(product.getPrice() + delta)
}
