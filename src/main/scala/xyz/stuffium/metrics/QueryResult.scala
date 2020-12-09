package xyz.stuffium.metrics

import org.apache.lucene.benchmark.quality.QualityStats

import scala.collection.mutable.ListBuffer

class QueryResult(judge: CFCJudge, sh: Int = 10) {

  val results = new ListBuffer[StatsResult]
  var p5, p10 = 0f
  var mrr = 0f

  def process(stats: QualityStats): Unit = {
    stats.getRecallPoints.foreach(x => {
      results.addOne(
        new StatsResult((x.getRecall/x.getRank).toFloat, (x.getRecall/stats.getMaxGoodPoints).toFloat, x.getRank)
      )
    })

    calcMrr(sh)
  }

  def genPRTable(): List[(Float, Float, Boolean)] = {
    val table = new ListBuffer[(Float, Float, Boolean)]

    for (ix <- 1 until 11) {
      val k = ix/10f
      val pl = results.filter(x => x.recall == k)

      if (pl.isEmpty) {
        interpolateAtRecallK(k) match {
          case Some(p) => table.addOne(k, p, false)
          case None => table.addOne(k, table.last._2, true)
        }
      } else {
        table.addOne(k, pl.head.precision, false)
      }
    }

    table.prepend((0f, table.head._2, true))
    table.toList
  }

  def interpolateAtRecallK(k: Float): Option[Float] = {
    val points = results
      .map(x => ((k - x.recall).abs, x))
      .sortBy(x => x._1)
      .map(x => x._2)
      .splitAt(2)
      ._1
      .sortBy(x => x.recall)

    if(points.isEmpty)
      return  None

    if(points.head.recall < k && points.last.recall > k) {
      val y = interpolate(
        points.head.recall, points.head.precision, points(1).recall, points(1).precision, 0.1f
      )

      Some(y)
    } else {
      None
    }
  }

  def calcMrr(sh: Int): Unit = {
    val lower = results.filter(x => x.rank <= sh)

    if (lower.isEmpty) {
      mrr = 0
    } else {
      mrr = 1f / lower.head.rank
    }
  }

  def interpolate(x1: Float, y1: Float, x3: Float, y3: Float, x2: Float): Float = {
    (x2 - x1) * (y3 - y1) / (x3 - x1) + y1
  }

  override def toString = s"QueryResult(p5=$p5, p10=$p10, mrr=$mrr)"

  class StatsResult(val precision: Float, val recall: Float, val rank: Int) {
    override def toString = s"StatsResult(precision=$precision, recall=$recall, rank=$rank)"
  }
}
























