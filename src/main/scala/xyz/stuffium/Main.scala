package xyz.stuffium

import java.io.IOException
import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field, StringField, TextField}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.{Directory, MMapDirectory}

object Main extends LazyLogging {

  org.slf4j.LoggerFactory
    .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[ch.qos.logback.classic.Logger]
    .setLevel(ch.qos.logback.classic.Level.TRACE)

  val analyzer: StandardAnalyzer = new StandardAnalyzer()
  val index: Directory = new MMapDirectory(Paths.get("db"))
  val config = new IndexWriterConfig(analyzer)
//  val reader: DirectoryReader = DirectoryReader.open(index)
//  val searcher = new IndexSearcher(reader)

  def main(args: Array[String]): Unit = {
    logger.info("Warp 10, engage")

    PreProcessor.importCFQueries()

//    val data = PreProcessor.importCFC()
//    insertData(data)
//
//
//    val querystr = "What are the effects of calcium on the physical properties of mucus from CF patients?"
//
//    val q = new QueryParser("text", analyzer).parse(querystr)
//
//    val hitsPerPage = 10
//    val reader = DirectoryReader.open(index)
//    val searcher = new IndexSearcher(reader)
//    val docs = searcher.search(q, hitsPerPage)
//    val hits = docs.scoreDocs
//
//    println(s"Found ${hits.length} documents")
//
//    hits.foreach(x => {
//      println(x.doc, searcher.doc(x.doc).getField("recordNumber").stringValue())
//    })

    logger.info("Say goodbye Data")
  }

  def insertData(data: List[(String, String)]): Unit = {
    val w = new IndexWriter(index, config)

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
