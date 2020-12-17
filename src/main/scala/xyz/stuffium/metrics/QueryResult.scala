package xyz.stuffium.metrics

import java.lang.reflect.Type

import com.google.gson.{JsonElement, JsonObject, JsonSerializationContext, JsonSerializer}
import org.apache.lucene.benchmark.quality.{QualityQuery, QualityStats}

import scala.collection.mutable.ListBuffer

class QueryResult(val qq: QualityQuery, sh: Int = 10) {

  val results = new ListBuffer[StatsResult]
  var precision, recall = 0f
  var mrr = 0f
  var rp = 0f
  var p5, p10 = 0f

  def process(stats: QualityStats): Unit = {
    stats.getRecallPoints.foreach(x => {
      results.addOne(
        new StatsResult((x.getRecall/x.getRank).toFloat, (x.getRecall/stats.getMaxGoodPoints).toFloat, x.getRank)
      )
    })

    precision = results.last.precision
    recall = results.last.recall
    p5 = stats.getPrecisionAt(5).toFloat
    p10 = stats.getPrecisionAt(10).toFloat

    calcRP(stats)
    calcMrr(sh)
  }

  def genPRTable(): List[TableEntry] = {
    val table = new ListBuffer[TableEntry]

    for (ix <- 1 until 11) {
      val k = ix/10f
      val pl = results.filter(x => x.recall == k)

      if (pl.isEmpty) {
        interpolateAtRecallK(k) match {
          case Some(p) => table.addOne(new TableEntry(k, p, false))
          case None => if(table.nonEmpty) table.addOne(new TableEntry(k, table.last.precision, true))
        }
      } else {
        table.addOne(new TableEntry(k, pl.head.precision, false))
      }
    }

    table.prepend(new TableEntry(0f, table.head.precision, true))
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
        points.head.recall, points.head.precision, points(1).recall, points(1).precision, k
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

  def calcRP(qs: QualityStats): Unit = {
    val n = qs
      .getRecallPoints
      .filter(x => x.getRank <= qs.getMaxGoodPoints)
      .last
      .getRecall
      .toFloat

    rp = n / qs.getMaxGoodPoints.toFloat
  }


  def interpolate(x1: Float, y1: Float, x3: Float, y3: Float, x2: Float): Float = {
    (x2 - x1) * (y3 - y1) / (x3 - x1) + y1
  }

  override def toString = s"QueryResult(precision=$precision, recall=$recall, mrr=$mrr)"

  class StatsResult(val precision: Float, val recall: Float, val rank: Int) {
    override def toString = s"StatsResult(precision=$precision, recall=$recall, rank=$rank)"
  }
}

object QueryResult extends JsonSerializer[QueryResult] {

  override def serialize(src: QueryResult, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
    val jo = new JsonObject

    jo.addProperty("queryID", src.qq.getQueryID)
    jo.addProperty("precision", src.precision)
    jo.addProperty("precisionAt5", src.p5)
    jo.addProperty("precisionAt10", src.p10)

    jo.addProperty("recall", src.recall)
    jo.addProperty("rp", src.rp)
    jo.addProperty("mrr", src.mrr)

    jo.add("PxRTable", context.serialize(src.genPRTable().toArray))

    jo
  }

}
