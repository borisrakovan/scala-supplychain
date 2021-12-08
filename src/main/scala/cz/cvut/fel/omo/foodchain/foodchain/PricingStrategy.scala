package cz.cvut.fel.omo.foodchain.foodchain

trait PricingStrategy {
  def apply(product: Tradeable): Double
}

class DefaultPricingStrategy {
  def apply(product: Tradeable): Double =
    product.getPrice() * 1.2
}
