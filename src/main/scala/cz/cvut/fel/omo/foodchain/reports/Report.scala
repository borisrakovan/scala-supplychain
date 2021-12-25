package cz.cvut.fel.omo.foodchain.reports

import cz.cvut.fel.omo.foodchain.utils.Tabulator

class Report(
    val title: String,
    val headers: List[String],
    val data: List[List[String]],
  ) {
  override def toString(): String =
    title.toUpperCase() + " REPORT\n" ++ Tabulator.format(headers +: data) ++ "\n"
}
