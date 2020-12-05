package xyz.stuffium.utils

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.benchmark.quality.{QualityQuery, QualityQueryParser}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.Query

class CFCQualityQueryParser(analyzer: Analyzer, field: String) extends QualityQueryParser {

  override def parse(qq: QualityQuery): Query = {
    new QueryParser(field, analyzer).parse(qq.getValue("queryText"))
  }

}
