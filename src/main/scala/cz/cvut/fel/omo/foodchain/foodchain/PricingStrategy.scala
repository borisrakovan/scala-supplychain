package cz.cvut.fel.omo.foodchain.foodchain

trait PricingStrategy {
  def apply(product: Tradeable): Double
}

class DefaultPricingStrategy extends PricingStrategy {
  def apply(product: Tradeable): Double = round(product.getPrice() * 1.2)

  def round(n: Double): Double = n - (n % 0.01)
}
