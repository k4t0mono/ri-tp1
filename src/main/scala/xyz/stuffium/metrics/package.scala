package xyz.stuffium

import java.io.{BufferedWriter, File, FileWriter}

import org.apache.lucene.benchmark.quality.{QualityQuery, QualityStats}

package object metrics {

  def exportResults(qss: List[QualityStats], qqs: List[QualityQuery], model: String, path: String): Unit = {
    val qrs = qss
      .zip(qqs)
      .map(x => {
        val qr = new QueryResult(x._2)
        qr.process(x._1)

        qr
      })

    val qs = QualityStats.average(qss.toArray)
    val mrr = qrs.map(x => x.mrr).sum / qrs.length

    val mr = new ModelReport(model, qrs, qs.getPrecisionAt(5).toFloat, qs.getPrecisionAt(10).toFloat, mrr)
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
