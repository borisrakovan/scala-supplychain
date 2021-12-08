package cz.cvut.fel.omo.foodchain.foodchain.channels

import cz.cvut.fel.omo.foodchain.foodchain.FoodChainParty

class PaymentChannel extends Channel
class PaymentRequest(
    channel: PaymentChannel,
    sender: FoodChainParty,
    val amount: Double,
    val payer: FoodChainParty,
  ) extends ChannelRequest(channel, sender) {}
