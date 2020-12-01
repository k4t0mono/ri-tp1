package xyz.stuffium

import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ListBuffer
import scala.io.Source

object PreProcessor extends LazyLogging {

  val cfc_files = List("cf74", "cf75", "cf76", "cf77", "cf78", "cf79")

  def importCFC(): List[(String, String)] = {
    val data = new ListBuffer[DataHolder]

    cfc_files.foreach(x => data.addAll(loadCFC(s"./data/$x")))

    data
      .map(x => (treatData(x.extractData()), x.recordNumber))
      .toList
  }

  def treatData(s: String): String = {
    s.toLowerCase
  }

  def loadCFC(path: String): List[DataHolder] = {
    logger.debug(s"importCFC($path}")

    val data: ListBuffer[DataHolder] = new ListBuffer[DataHolder]

    val buff = Source.fromFile(path)

    var dh = new DataHolder()
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
        case "RN " => dh.recordNumber = line
        case "AN " => dh.accessionNumber = line
        case "AU " => dh.authors = line
        case "TI " => dh.updateTitle(line); ti_flag = true
        case "SO " => dh.source = line; ti_flag = false
        case "MJ " => dh.updateMajorSubjects(line); mj_flag = true
        case "MN " => dh.updateMinorSubjects(line); mj_flag = false; mn_flag = true
        case "AB " => dh.updateAbstract(line); mn_flag = false; ab_flag = true
        case "RF " => dh.updateCitation(line); ab_flag = false; rf_flag = true
        case "CT " => dh.updateCitation(line); rf_flag = false; ct_flag = true
        case _ =>
          if(ti_flag) dh.updateTitle(line)
          if(mj_flag) dh.updateMajorSubjects(line)
          if(mn_flag) dh.updateMinorSubjects(line)
          if(ab_flag) dh.updateAbstract(line)
          if(rf_flag) dh.updateReferences(line)
          if(ct_flag) dh.updateCitation(line)
      }

      if(x.isEmpty) {
        ct_flag = false
        dh.complete()
        data.addOne(dh)
        logger.trace(s"Found new article $dh")

        dh = new DataHolder()
      }
    })

    logger.debug(s"Found ${data.size} articles")
    data.toList
  }

}
