package cz.cvut.fel.omo.foodchain

import cz.cvut.fel.omo.foodchain.utils.Tabulator
import cz.cvut.fel.omo.foodchain.reports.Report

trait UserInterface {
  def showText(line: String): Unit
  def showReport(report: Report): Unit
  def getUserSelection(prompt: String, options: List[String]): String
  def getYesNoAnswer(prompt: String): Boolean
  def requireConfirmation(): Unit
}

class CommandLineInterface extends UserInterface {
  override def showText(line: String): Unit =
    println(line)
  // Console.out.flush()

  override def showReport(report: Report): Unit = {
    showText(report.title.toUpperCase() + " REPORT")
    showText(Tabulator.format(report.headers +: report.data))
  }

  override def requireConfirmation(): Unit = {
    showText("Press Enter to continue")
    val r = System.console().reader()
    val _ = r.read.toChar
    // val _ =
    // scala.io.StdIn.readChar()
  }

  override def getYesNoAnswer(prompt: String): Boolean = {
    showText(s"$prompt (y/n)")
    scala.io.StdIn.readBoolean()
  }

  override def getUserSelection(prompt: String, options: List[String]): String = {
    showText(prompt)
    val optionsStr =
      options.zipWithIndex.map { case (opt, i) => s"[${(i + 1).toString()}] $opt" }.mkString(" ")

    @scala.annotation.tailrec
    def getSelection(): String = {
      showText(optionsStr)
      try {
        val selection = scala.io.StdIn.readInt()
        if ((1 to options.length).contains(selection))
          options(selection - 1)
        else
          throw new NumberFormatException(selection.toString())
      }
      catch {
        case e: NumberFormatException =>
          showText(s"Invalid option: ${e.getMessage()}")
          getSelection()
      }
    }

    getSelection()
  }
}
