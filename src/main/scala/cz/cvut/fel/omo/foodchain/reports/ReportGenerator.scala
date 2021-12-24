package cz.cvut.fel.omo.foodchain.reports

import cz.cvut.fel.omo.foodchain.ecosystem.Ecosystem
import cz.cvut.fel.omo.foodchain.utils.Utils
import cz.cvut.fel.omo.foodchain.foodchain.transactions.FoodTransaction
import cz.cvut.fel.omo.foodchain.foodchain.operations.FoodOperation
import cz.cvut.fel.omo.foodchain.foodchain.FoodMaterial
import cz.cvut.fel.omo.foodchain.blockchain.BlockChain
import cz.cvut.fel.omo.foodchain.foodchain.operations.TransferOperation

abstract class ReportGenerator {
  def generate(ecosystem: Ecosystem): Report
}

class PartiesReportGenerator extends ReportGenerator {
  override def generate(ecosystem: Ecosystem): Report = {
    val headers = List("Party", "Balance", "Blockchain size", "Food materials")

    val data = ecosystem.parties.map { p =>
      List(
        p.id,
        Utils.formatPrice(p.balance).toString(),
        p.blockChain.numBlocks().toString(),
        "[" + p.foodRepo.getAll().map(_.ofType).mkString(", ") + "]",
      )
    }

    new Report("Parties", headers, data)
  }
}

class MaterialsReportGenerator extends ReportGenerator {
  override def generate(ecosystem: Ecosystem): Report = {
    val party = ecosystem.getTrustedParty()
    val blockChain = party.blockChain

    val headers = List("Party", "Operation", "Time")

    val data: List[List[String]] =
      ecosystem
        .foodMaterials
        .foldLeft(List[List[String]]())(generateNext(blockChain))

    new Report("Materials", headers, data)
  }

  private def generateNext(
      blockChain: BlockChain
    )(
      acc: List[List[String]],
      material: FoodMaterial,
    ): List[List[String]] = {
    val materialTransactions = blockChain.flatTransactions().filter {
      case t: FoodTransaction =>
        t.operation match {
          case op: FoodOperation => op.materials.contains(material)
          case op: TransferOperation => op.materials.contains(material)
          case _ => false
        }
      case _ => false
    }

    val current = materialTransactions.map { t =>
      List(
        t.initiator.id,
        t.operation.toString(),
        t.time.toString(),
      )
    }
    acc :::
      List("") ::
      List(material.ofType.toUpperCase()) ::
      current
  }
}

class TransactionsReportGenerator extends ReportGenerator {
  override def generate(ecosystem: Ecosystem): Report = {
    val party = ecosystem.getTrustedParty()
    val blockChain = party.blockChain

    val headers = List("ID", "Time", "Initiator", "Operation")

    val data: List[List[String]] = blockChain.flatTransactions().map { tx =>
      List(
        tx.id,
        tx.time.toString(),
        tx.initiator.id,
        tx.operation.toString(),
      )
    }

    new Report("Transactions", headers, data)
  }
}

class SecurityReportGenerator extends ReportGenerator {
  override def generate(ecosystem: Ecosystem): Report = {
    val party = ecosystem.getTrustedParty()

    val headers = List("Party", "Violation", "Description", "Time")

    val data: List[List[String]] = party.securityLog.getViolations().map { violation =>
      List(
        violation.node.id,
        violation.getClass().getSimpleName(),
        violation.description,
        violation.time.toString(),
      )
    }

    new Report("Security", headers, data)
  }
}
