package xyz.stuffium.importer

import scala.collection.mutable.ListBuffer

class DocumentHolder(
                  var paperNumber: String, var recordNumber: String, var accessionNumber: String,
                  var authors: String, var title: String, var source: String, var majorSubjects: String,
                  var minorSubjects: String, var _abstract: String, var references: String,
                  var citations: String) {

  def this() = this("", "", "", "", "", "", "", "", "", "", "")

  def extractData(): String = {
    s"${authors} ${title} ${source} ${majorSubjects} ${minorSubjects} ${_abstract}"
  }

  override def toString: String = s"<DataHolder PN=$paperNumber />"

  val title_s: ListBuffer[String] = new ListBuffer[String]
  def updateTitle(s: String): Unit = title_s.addOne(s)

  val majorSubjects_s: ListBuffer[String] = new ListBuffer[String]
  def updateMajorSubjects(s: String): Unit = majorSubjects_s.addOne(s)

  val minorSubjects_s: ListBuffer[String] = new ListBuffer[String]
  def updateMinorSubjects(s: String): Unit = minorSubjects_s.addOne(s)

  val abstract_s: ListBuffer[String] = new ListBuffer[String]
  def updateAbstract(s: String): Unit = abstract_s.addOne(s)

  val references_s: ListBuffer[String] = new ListBuffer[String]
  def updateReferences(s: String): Unit = references_s.addOne(s)

  val citation_s: ListBuffer[String] = new ListBuffer[String]
  def updateCitation(s: String): Unit = citation_s.addOne(s)

  def complete(): Unit = {
    title = title_s.mkString(" ")
    title_s.clear()

    majorSubjects = majorSubjects_s.mkString(" ")
    majorSubjects_s.clear()

    minorSubjects = minorSubjects_s.mkString(" ")
    minorSubjects_s.clear()

    _abstract = abstract_s.mkString(" ")
    abstract_s.clear()

    references = references_s.mkString(" ")
    references_s.clear()

    citations = citation_s.mkString(" ")
    citation_s.clear()
  }

}