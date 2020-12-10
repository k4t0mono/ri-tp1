package xyz.stuffium

import java.io.{BufferedWriter, File, FileWriter}

import com.typesafe.scalalogging.LazyLogging
import org.apache.lucene.benchmark.quality.{QualityQuery, QualityStats}

package object metrics extends LazyLogging {

  def getRPModels(mr1: ModelReport, mr2: ModelReport): RPReport = {
    val results = mr1
      .results
      .zip(mr2.results)
      .map(x => x._1.rp - x._2.rp)

    new RPReport(mr1.model, mr2.model, results)
  }

  def exportRPReport(rpr: RPReport, path: String): Unit = {
    logger.info(s"Exporting R-Precision Report")

    val s = Main.gson.toJson(rpr)
    writeFile(path, s)
  }

  def getModelReport(qss: List[QualityStats], qqs: List[QualityQuery], model: String): ModelReport = {
    val qrs = qss
      .zip(qqs)
      .map(x => {
        val qr = new QueryResult(x._2)
        qr.process(x._1)

        qr
      })

    val qs = QualityStats.average(qss.toArray)
    val mrr = qrs.map(x => x.mrr).sum / qrs.length

    new ModelReport(model, qrs, qs.getPrecisionAt(5).toFloat, qs.getPrecisionAt(10).toFloat, mrr)
  }

  def exportModelReport(mr: ModelReport, path: String): Unit = {
    logger.info(s"Exporting results of the model ${mr.model} to the file $path")

    val s = Main.gson.toJson(mr)
    writeFile(path, s)
  }

  def writeFile(filename: String, s: String): Unit = {
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(s)
    bw.close()
  }

}
