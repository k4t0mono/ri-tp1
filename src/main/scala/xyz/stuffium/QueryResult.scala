package xyz.stuffium

import org.apache.lucene.benchmark.quality.QualityQuery
import org.apache.lucene.search.TopDocs
import xyz.stuffium.metrics.CFCJudge

import scala.collection.mutable.ListBuffer

class QueryResult(judge: CFCJudge) {

  val results = new ListBuffer[StatsResult]
  var p5, p10 = 0f
  var mrr = 0f

  def process(qq: QualityQuery, docs: TopDocs): Unit = {
    var tp = 0
    docs
      .scoreDocs
      .zipWithIndex
      .foreach(x => {
        if (judge.isRelevant(x._1.doc, qq)) {
          tp += 1

          val p = tp / (x._2 + 1).toFloat
          val r = tp / judge.maxRecall(qq).toFloat
          results.addOne(new StatsResult(p, r, x._2))
        }

        if(x._2 == 4) {
          p5 = tp / (x._2 + 1).toFloat
        }

        if(x._2 == 9) {
          p10 = tp / (x._2 + 1).toFloat
        }
      })

    calc_mrr()
  }

  def calc_mrr(sh: Int = 10): Unit = {
    val lower = results.filter(x => x.index <= 10)

    if (lower.isEmpty) {
      mrr = 0
      return
    }

    mrr = 1f / lower.head.index
  }

  class StatsResult(val precision: Float, val recall: Float, val index: Int) {
    override def toString = s"StatsResult(precision=$precision, recall=$recall, index=$index)"
  }

  override def toString = s"QueryResult(p5=$p5, p10=$p10, mrr=$mrr)"
}

object QueryResult {

  def process2(): Unit = {

  }

}


























