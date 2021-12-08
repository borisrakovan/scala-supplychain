package cz.cvut.fel.omo.foodchain.ecosystem

case object EcosystemConfig {
  val foodMaterialIds: List[String] =
    List(
      "apple",
      "pear",
      "melon",
      "strawberry",
      "banana",
      "tomato",
      "potato",
      "cucumber",
      "salad",
      "garden salad",
      "cabbage",
      "egg",
      "milk",
      "cheesse",
      "suggar beet",
      "suggar",
      "salt",
      "chocolate",
      "apple pie",
      "fries",
    )

  val allowedMaterialTransitions: Map[List[String], List[String]] = Map(
    List("apple", "suggar") -> List("apple pie"),
    List("tomato", "salt") -> List("fries"),
    List("milk") -> List("cheese"),
    List("milk") -> List("cheese"),
    List("tomato", "salad") -> List("garden salad"),
  )
}
