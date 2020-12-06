package xyz.stuffium

import java.io.IOException
import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.benchmark.quality.QualityBenchmark
import org.apache.lucene.document.{Document, Field, StringField, TextField}
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{IndexSearcher, TopDocs}
import org.apache.lucene.store.MMapDirectory
import xyz.stuffium.importer.CFCImporter
import xyz.stuffium.metrics.CFCJudge
import xyz.stuffium.utils.CFCQualityQueryParser

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
  val hits = 100
  val fileNameField = "recordNumber"
  val textField = "text"

  def main(args: Array[String]): Unit = {
    logger.info("Warp 10, engage")

    index = Some(new MMapDirectory(Paths.get("db")))
    config = Some(new IndexWriterConfig(analyzer))

    val (qqs, cjs) = CFCImporter.importCFQueries()
    val data = CFCImporter.importCFC()
    insertData(data)

    reader = Some(DirectoryReader.open(index.get))
    searcher = Some(new IndexSearcher(reader.get))

    val judge = new CFCJudge
    judge.addJudgments(cjs)

    val qqp = new CFCQualityQueryParser(analyzer, textField)
    val qrun = new QualityBenchmark(qqs.toArray, qqp, searcher.get, fileNameField)

    import java.io.PrintWriter
    val logger2 = new PrintWriter("log")
    val stats = qrun.execute(judge, null, logger2)

    stats
      .head
      .getRecallPoints
      .foreach(x=> {
        println(x.getRecall, x.getRank)
      })


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

    doc.add(new TextField(textField, text, Field.Store.YES))
    doc.add(new StringField(fileNameField, recordNumber, Field.Store.YES))

    w.addDocument(doc)
  }

}
