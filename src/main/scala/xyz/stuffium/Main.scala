package xyz.stuffium

import java.io.{IOException, PrintWriter}
import java.nio.file.Paths

import com.google.gson.{Gson, GsonBuilder}
import com.typesafe.scalalogging.LazyLogging
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.benchmark.quality.utils.SubmissionReport
import org.apache.lucene.benchmark.quality.{QualityBenchmark, QualityQuery, QualityStats}
import org.apache.lucene.document.{Document, Field, StringField, TextField}
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.similarities.BM25Similarity
import org.apache.lucene.search.{IndexSearcher, TopDocs}
import org.apache.lucene.store.MMapDirectory
import xyz.stuffium.importer.CFCImporter
import xyz.stuffium.metrics.{CFCJudge, ModelReport, QueryResult, RPReport, TableEntry}
import xyz.stuffium.utils.{CFCQualityQueryParser, VectorSimilarity}

object Main extends LazyLogging {

  org.slf4j.LoggerFactory
    .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[ch.qos.logback.classic.Logger]
    .setLevel(ch.qos.logback.classic.Level.INFO)

  val analyzer: StandardAnalyzer = new StandardAnalyzer()
  val hits = 1300
  val fileNameField = "recordNumber"
  val textField = "text"

  val index_vector = new MMapDirectory(Paths.get("db", "vector"))
  val index_bm25 = new MMapDirectory(Paths.get("db", "bm25"))

  val gson: Gson = new GsonBuilder()
    .setPrettyPrinting()
    .registerTypeHierarchyAdapter(classOf[ModelReport], ModelReport)
    .registerTypeHierarchyAdapter(classOf[QueryResult], QueryResult)
    .registerTypeHierarchyAdapter(classOf[TableEntry], TableEntry)
    .registerTypeHierarchyAdapter(classOf[RPReport], RPReport)
    .create()

  def main(args: Array[String]): Unit = {
    logger.info("Warp 10, engage")

    val (qqs, cjs) = CFCImporter.importCFQueries()
    val data = CFCImporter.importCFC()

//    storeVector(data)
//    storeBM25(data)

    val judge = new CFCJudge
    judge.addJudgments(cjs)
    val qqp = new CFCQualityQueryParser(analyzer, textField)

    val sv = testVector(qqs.toArray, qqp, judge)
    val mrv = metrics.getModelReport(sv, qqs, "vectorial")

    val sp = testBM25(qqs.toArray, qqp, judge)
    val mrp = metrics.getModelReport(sp, qqs, "bm25")

    metrics.exportModelReport(mrv, "report_vec.json")
    metrics.exportModelReport(mrp, "report_bm25.json")

    val rpr = metrics.getRPModels(mrv, mrp)
    metrics.exportRPReport(rpr, "rp_report.json")

    logger.info("Say goodbye Data")
  }

  def searchVector(qq: QualityQuery): TopDocs = {
    val is = getVectorSearcher
    val q = new QueryParser(textField, analyzer).parse(qq.getValue("queryText"))

    is.search(q, hits)
  }

  def testVector(qqs: Array[QualityQuery], qqp: CFCQualityQueryParser, judge: CFCJudge): List[QualityStats] = {
    logger.info("Testing the vectorial model")
    val qrun = new QualityBenchmark(qqs, qqp, getVectorSearcher, fileNameField)
    val lg = new PrintWriter("log_vector")
    val stats = qrun.execute(judge, null, lg)

    qrun.execute(null, new SubmissionReport(new PrintWriter("sr_vector"), "test"), lg)

    stats.toList
  }

  def testBM25(qqs: Array[QualityQuery], qqp: CFCQualityQueryParser, judge: CFCJudge): List[QualityStats] = {
    logger.info("Testing the BM25 model")
    val qrun = new QualityBenchmark(qqs, qqp, getBM25Searcher, fileNameField)
    val lg = new PrintWriter("log_bm25")
    val stats = qrun.execute(judge, null, lg)

    qrun.execute(null, new SubmissionReport(new PrintWriter("sr_bm25"), "test"), lg)

    stats.toList
  }

  def getBM25Searcher: IndexSearcher = {
    val reader = DirectoryReader.open(index_bm25)

    val is = new IndexSearcher(reader)
    is.setSimilarity(new BM25Similarity)

    is
  }

  def getVectorSearcher: IndexSearcher = {
    val reader = DirectoryReader.open(index_vector)

    val is = new IndexSearcher(reader)
    is.setSimilarity(new VectorSimilarity)

    is
  }

  def storeBM25(data: List[(String, String)]): Unit = {
    logger.info("Storing on BM25 model")
    val iwc = new IndexWriterConfig(analyzer)
    iwc.setSimilarity(new BM25Similarity)

    insertData(data, index_bm25, iwc)
  }

  def storeVector(data: List[(String, String)]): Unit = {
    logger.info("Storing on vector model")
    val iwc = new IndexWriterConfig(analyzer)
    iwc.setSimilarity(new VectorSimilarity)

    insertData(data, index_vector, iwc)
  }

  def insertData(data: List[(String, String)], index: MMapDirectory, config: IndexWriterConfig): Unit = {
    val w = new IndexWriter(index, config)

    data.foreach(x => addDoc(w, x._1, x._2))

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
