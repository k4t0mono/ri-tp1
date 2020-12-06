package xyz.stuffium.utils

import org.apache.lucene.search.similarities.TFIDFSimilarity

import scala.math.log

class VectorSimilarity extends TFIDFSimilarity {
  override def tf(freq: Float): Float = {
    log(1 + freq).toFloat
  }

  override def idf(docFreq: Long, docCount: Long): Float = {
    log(1 + (docCount - docFreq + 0.5)/(docFreq + 0.5)).toFloat
  }

  override def lengthNorm(length: Int): Float = log(length).toFloat
}
