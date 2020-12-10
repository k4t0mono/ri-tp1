package xyz.stuffium.utils

import org.apache.lucene.search.similarities.TFIDFSimilarity

import scala.math.log

class VectorSimilarity extends TFIDFSimilarity {

  override def tf(freq: Float): Float = {
    1f + log(freq).toFloat
  }

  override def idf(docFreq: Long, docCount: Long): Float = {
    log(1 + docCount/docFreq).toFloat
  }

  override def lengthNorm(length: Int): Float = log(length).toFloat

}
