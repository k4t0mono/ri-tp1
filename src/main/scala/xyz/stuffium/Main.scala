package xyz.stuffium

import java.io.IOException
import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field, StringField, TextField}
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{IndexSearcher, TopDocs}
import org.apache.lucene.store.MMapDirectory
import xyz.stuffium.importer.CFCImporter

object Main extends LazyLogging {

  org.slf4j.LoggerFactory
    .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[ch.qos.logback.classic.Logger]
    .setLevel(ch.qos.logback.classic.Level.INFO)

  val analyzer: StandardAnalyzer = new StandardAnalyzer()
  var index: Option[MMapDirectory] = None
  var config: Option[IndexWriterConfig] = None
  var reader: Option[DirectoryReader] = None
  var searcher: Option[IndexSearcher] = None
  val hits = 10

  def main(args: Array[String]): Unit = {
    logger.info("Warp 10, engage")

    index = Some(new MMapDirectory(Paths.get("db")))
    config = Some(new IndexWriterConfig(analyzer))

    val queries = CFCImporter.importCFQueries()
    val data = CFCImporter.importCFC()
    insertData(data)

    reader = Some(DirectoryReader.open(index.get))
    searcher = Some(new IndexSearcher(reader.get))

    val qh = queries(69)
    val results = query(qh.queryText)
    logger.info(s"Found ${results.totalHits} for the query $qh")
    results
      .scoreDocs
      .splitAt(10)
      ._1
      .zip(qh.relevantDocuments)
      .map(x => (x._1.doc, x._2.number()))
      .foreach(println)

    logger.info("Say goodbye Data")
  }

  def query(q: String, field: String = "text"): TopDocs = {
    val query = new QueryParser(field, analyzer).parse(q)

    searcher.get.search(query, hits)
  }

  def insertData(data: List[(String, String)]): Unit = {
    val w = new IndexWriter(index.get, config.get)

    data
      .foreach(x => addDoc(w, x._1, x._2))

    w.close()
  }

  @throws[IOException]
  private def addDoc(w: IndexWriter, text: String, recordNumber: String): Unit = {
    val doc = new Document()

    doc.add(new TextField("text", text, Field.Store.YES))
    doc.add(new StringField("recordNumber", recordNumber, Field.Store.YES))

    w.addDocument(doc)
  }

}
