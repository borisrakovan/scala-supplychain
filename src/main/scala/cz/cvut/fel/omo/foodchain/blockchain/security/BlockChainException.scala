package cz.cvut.fel.omo.foodchain.blockchain.security

class BlockChainException(message: String) extends Exception(message)

class AlreadyMinedException
    extends BlockChainException("Block was already mined by some other node")
