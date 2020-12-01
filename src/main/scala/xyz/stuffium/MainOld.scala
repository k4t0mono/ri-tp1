package stuffium

import java.io.IOException
import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field, StringField, TextField}
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.MMapDirectory

object MainOld extends LazyLogging {

  org.slf4j.LoggerFactory
    .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[ch.qos.logback.classic.Logger]
    .setLevel(ch.qos.logback.classic.Level.TRACE)

//  val analyzer: StandardAnalyzer = new StandardAnalyzer()
//  val index: Directory = new MMapDirectory(Paths.get("m"))
//  val reader: DirectoryReader = DirectoryReader.open(index)
//  val searcher = new IndexSearcher(reader)

  def main(args: Array[String]): Unit = {
    logger.info("Warp 10, engage")
    val analyzer = new StandardAnalyzer

    // 1. create the index
    val index = new MMapDirectory(Paths.get("db"))

    val config = new IndexWriterConfig(analyzer)

    val w = new IndexWriter(index, config)
    addDoc(w, "Lucene in Action", "193398817")
    addDoc(w, "Lucene for Dummies", "55320055Z")
    addDoc(w, "Managing Gigabytes", "55063554A")
    addDoc(w, "The Art of Computer Science", "9900333X")
    w.close()

    // 2. query
    val querystr = "lucene art man"

    // the "title" arg specifies the default field to use
    // when no field is explicitly specified in the query.
    val q = new QueryParser("title", analyzer).parse(querystr)

    // 3. search
    val hitsPerPage = 10
    val reader = DirectoryReader.open(index)
    val searcher = new IndexSearcher(reader)
    val docs = searcher.search(q, hitsPerPage)
    val hits = docs.scoreDocs

    // 4. display results
    System.out.println("Found " + hits.length + " hits.")
    for (i <- 0 until hits.length) {
      val docId = hits(i).doc
      val d = searcher.doc(docId)
      System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"))
    }

    // reader can only be closed when there
    // is no need to access the documents any more.

    logger.info("Say goodbye Data")
  }

//  def addToIndex(): Unit = {
//    val config = new IndexWriterConfig(analyzer)
//    val w = new IndexWriter(index, config)
//
//    addDoc(w, "Lucene in Action", "193398817")
//    addDoc(w, "Lucene for Dummies", "55320055Z")
//    addDoc(w, "Managing Gigabytes", "55063554A")
//    addDoc(w, "The Art of Computer Science", "9900333X")
//
//    w.close()
//  }

  @throws[IOException]
  private def addDoc(w: IndexWriter, title: String, isbn: String): Unit = {
    val doc = new Document()

    doc.add(new TextField("title", title, Field.Store.YES))
    doc.add(new StringField("isbn", isbn, Field.Store.YES))

    w.addDocument(doc)
  }

}
