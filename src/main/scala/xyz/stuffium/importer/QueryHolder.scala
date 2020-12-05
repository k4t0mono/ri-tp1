package xyz.stuffium.importer

import java.util

import org.apache.lucene.benchmark.quality.QualityQuery
import xyz.stuffium.metrics.CFCJudgement

import scala.collection.mutable.ListBuffer

class QueryHolder(var queryNumber: Int, var queryText: String, var relevantDocuments: List[RelevantDocument]) {

  def this() = this(0, "", List())

  val query_s = new ListBuffer[String]
  def updateQuery(s: String): Unit = {
    query_s.addOne(s)
  }

  val relevant_s = new ListBuffer[String]
  def updateRelevant(s: String): Unit = {
    relevant_s.addOne(s)
  }

  def complete(): Unit = {
    queryText = query_s.mkString(" ")
    query_s.clear()

    val doc_pattern = "((\\d{1,4}) (\\d{4}))".r
    relevantDocuments = doc_pattern
      .findAllIn(relevant_s.mkString(" "))
      .map(x => x.split(" "))
      .map(x => (x(0).toInt, x(1)))
      .map(x => new RelevantDocument(x._1, x._2))
      .toList
  }


  def convert: (QualityQuery, CFCJudgement) = {
    val fields = new util.HashMap[String, String]()
    fields.put("queryText", queryText)

    val qq = new QualityQuery(queryNumber.toString, fields)
    val cj = new CFCJudgement(queryNumber.toString, relevantDocuments)
    relevantDocuments.foreach(x => cj.addDocument(x))

    (qq, cj)
  }

  override def toString = s"<QueryHolder QN=$queryNumber NR=${relevantDocuments.length} />"
}