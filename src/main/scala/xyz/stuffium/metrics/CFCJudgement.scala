package xyz.stuffium.metrics

import java.lang.reflect.Type

import com.google.gson.{JsonElement, JsonObject, JsonSerializationContext, JsonSerializer}
import com.typesafe.scalalogging.LazyLogging
import xyz.stuffium.importer.RelevantDocument

import scala.collection.mutable

class CFCJudgement(queryID: String, relevantDocuments: List[RelevantDocument]) extends LazyLogging {

  val documents = new mutable.HashMap[String, RelevantDocument]()

  def addDocument(rd: RelevantDocument): Unit = {
    documents.addOne(rd.number().toString, rd)
  }

  def isRelevant(doc_number: String): Boolean = {
    documents.contains(doc_number)
  }

  def maxRecall: Int = documents.size

  def queryID(): String = this.queryID

  def relevantDocuments(): List[RelevantDocument] = this.relevantDocuments

  def relevantDocument(i: Int): RelevantDocument = this.relevantDocuments(i)


  override def toString = s"<CFCJudgement queryID=$queryID />"
}

object CFCJudgement extends JsonSerializer[CFCJudgement] {

  override def serialize(src: CFCJudgement, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
    val jo = new JsonObject

    val docs = src
      .documents
      .toList
      .map(x => x._1)
      .sorted
      .toArray

    jo.addProperty("id", src.queryID())
    jo.add("documents", context.serialize(docs))

    jo
  }

}