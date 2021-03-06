package xyz.stuffium.metrics

import java.io.PrintWriter
import java.lang.reflect.Type

import com.google.gson.{JsonElement, JsonObject, JsonSerializationContext, JsonSerializer}
import org.apache.lucene.benchmark.quality.{Judge, QualityQuery}

import scala.collection.mutable

class CFCJudge extends Judge {
  val judgements = new mutable.HashMap[String, CFCJudgement]()

  def addJudgments(cjs: List[CFCJudgement]): Unit = {
    cjs.foreach(x => judgements.addOne(x.queryID(), x))
  }

  override def isRelevant(docName: String, query: QualityQuery): Boolean = {
    judgements.get(query.getQueryID) match {
      case None => false
      case Some(x) => x.isRelevant(docName)
    }
  }

  def isRelevant(docName: Int, query: QualityQuery): Boolean = {
    isRelevant(docName.toString, query)
  }

  override def validateData(qq: Array[QualityQuery], logger: PrintWriter): Boolean = true

  override def maxRecall(query: QualityQuery): Int = {
    judgements.get(query.getQueryID) match {
      case Some(x) => x.maxRecall
      case None => 0
    }
  }
}

object CFCJudge extends JsonSerializer[CFCJudge] {
  override def serialize(src: CFCJudge, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
    val jo = new JsonObject

    val juds = src
      .judgements
      .toList
      .map(x => x._2)
      .toArray

    jo.add("queries", context.serialize(juds))

    jo
  }
}
