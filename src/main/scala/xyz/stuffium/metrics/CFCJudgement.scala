package xyz.stuffium.metrics

import com.typesafe.scalalogging.LazyLogging
import xyz.stuffium.importer.RelevantDocument

import scala.collection.mutable

class CFCJudgement(queryID: String, relevantDocuments: List[RelevantDocument]) extends LazyLogging {

  val documents = new mutable.HashMap[String, RelevantDocument]()

  def addDocument(rd: RelevantDocument): Unit = {
    documents.addOne(rd.number().toString, rd)
  }

  def isRelevant(doc_number: String): Boolean = {
    logger.info(s"isRelevant: $doc_number")
    documents.contains(doc_number)
  }

  def maxRecall: Int = documents.size

  def queryID(): String = this.queryID

  def relevantDocuments(): List[RelevantDocument] = this.relevantDocuments

  def relevantDocument(i: Int): RelevantDocument = this.relevantDocuments(i)


  override def toString = s"<CFCJudgement queryID=$queryID />"
}