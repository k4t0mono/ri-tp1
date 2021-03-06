package xyz.stuffium.importer

import com.typesafe.scalalogging.LazyLogging
import xyz.stuffium.PreProcessor
import xyz.stuffium.PreProcessor.treatData

import scala.collection.mutable.ListBuffer
import scala.io.Source
import org.apache.lucene.benchmark.quality.QualityQuery
import xyz.stuffium.metrics.CFCJudgement

object CFCImporter extends LazyLogging {

  val cfc_files = List("cf74", "cf75", "cf76", "cf77", "cf78", "cf79")
  val cfc_queries = List("cfquery")

  def importCFC(): List[(String, String)] = {
    val data = new ListBuffer[DocumentHolder]

    cfc_files.foreach(x => data.addAll(loadCFCData(s"./data/$x")))
    logger.info(s"Found ${data.length} articles")

    data
      .map(x => (PreProcessor.treatData(x.extractData()), x.recordNumber))
      .toList
  }

  def importCFQueries(): (List[QualityQuery], List[CFCJudgement]) = {
    val data = new ListBuffer[QueryHolder]

    cfc_queries.foreach(x => data.addAll(loadCFCQuery(s"./data/$x")))
    logger.info(s"Found ${data.length} queries")

    val qqs = new ListBuffer[QualityQuery]
    val cjs = new ListBuffer[CFCJudgement]

    data
      .map(x => x.convert)
      .foreach(x => {
        qqs.addOne(x._1)
        cjs.addOne(x._2)
      })

    (qqs.toList, cjs.toList)
  }

  def loadCFCQuery(path: String): List[QueryHolder] = {
    logger.debug(s"loadCFCQuery($path}")

    val data = new ListBuffer[QueryHolder]
    val buff = Source.fromFile(path)

    var qh = new QueryHolder()
    var qu_flag = false
    var rd_flag = false
    buff.getLines().foreach(x => {
      val (code, line) = x.splitAt(3)

      code match {
        case "QN " => qh.queryNumber = line.strip().toInt
        case "QU " => qh.updateQuery(line); qu_flag = true
        case "NR " => qu_flag = false
        case "RD " => qh.updateRelevant(line); rd_flag = true
        case _ =>
          if (qu_flag) qh.updateQuery(line)
          if (rd_flag) qh.updateRelevant(line)
      }

      if (x.strip().isEmpty) {
        rd_flag = false
        qh.complete()

        qh.queryText = treatData(qh.queryText)

        data.addOne(qh)
        logger.trace(s"Found new query: $qh")

        qh = new QueryHolder()
      }
    })

    logger.debug(s"Found ${data.size} queries")
    data.toList
  }

  def loadCFCData(path: String): List[DocumentHolder] = {
    logger.debug(s"loadCFCData($path}")

    val data = new ListBuffer[DocumentHolder]
    val buff = Source.fromFile(path)

    var dh = new DocumentHolder()
    var ti_flag = false
    var mj_flag = false
    var mn_flag = false
    var ab_flag = false
    var rf_flag = false
    var ct_flag = false

    buff.getLines().foreach(x => {
      val (code, line) = x.splitAt(3)

      code match {
        case "PN " => dh.paperNumber = line
        case "RN " => dh.recordNumber = line.strip().toInt.toString
        case "AN " => dh.accessionNumber = line
        case "AU " => dh.authors = line
        case "TI " => dh.updateTitle(line); ti_flag = true
        case "SO " => dh.source = line; ti_flag = false
        case "MJ " => dh.updateMajorSubjects(line); mj_flag = true
        case "MN " => dh.updateMinorSubjects(line); mj_flag = false; mn_flag = true
        case "AB " => dh.updateAbstract(line); mn_flag = false; ab_flag = true
        case "EX " => dh.updateAbstract(line); mn_flag = false; ab_flag = true
        case "RF " => dh.updateCitation(line); ab_flag = false; rf_flag = true
        case "CT " => dh.updateCitation(line); rf_flag = false; ct_flag = true
        case _ =>
          if (ti_flag) dh.updateTitle(line)
          if (mj_flag) dh.updateMajorSubjects(line)
          if (mn_flag) dh.updateMinorSubjects(line)
          if (ab_flag) dh.updateAbstract(line)
          if (rf_flag) dh.updateReferences(line)
          if (ct_flag) dh.updateCitation(line)
      }

      if (x.isEmpty) {
        ct_flag = false
        dh.complete()
        data.addOne(dh)
        logger.trace(s"Found new article: $dh")

        dh = new DocumentHolder()
      }
    })

    logger.debug(s"Found ${data.size} articles")
    data.toList
  }

}
