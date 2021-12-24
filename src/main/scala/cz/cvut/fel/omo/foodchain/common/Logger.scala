package cz.cvut.fel.omo.foodchain.common

trait LogSource {
  val id: String
}

sealed abstract class LogLevel(val name: String, val color: String)
case object Info extends LogLevel("INFO", Console.GREEN)
case object Warn extends LogLevel("WARN", Console.YELLOW)
case object Error extends LogLevel("ERROR", Console.RED)

object Logger {
  def log(
      msg: String,
      source: LogSource = null,
      level: LogLevel = Info,
    ): Unit = {
    val src = if (source != null) s"<${source.id}> " else ""
    println(s"[${level.color + level.name + Console.RESET}] $src$msg")
  }

  def info(msg: String, source: LogSource = null): Unit =
    log(msg, source, Info)
  def error(msg: String, source: LogSource = null): Unit = log(msg, source, Error)
  def warn(msg: String, source: LogSource = null): Unit = log(msg, source, Warn)
}
