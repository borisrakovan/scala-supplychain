package cz.cvut.fel.utils

class IdGenerator {
  private var id = 0

  def getNextId(): String = {
    id += 1
    id.toString
  }
}
