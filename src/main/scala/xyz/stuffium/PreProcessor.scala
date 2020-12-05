package xyz.stuffium

import java.io.FileInputStream

import com.typesafe.scalalogging.LazyLogging
import opennlp.tools.tokenize.{TokenizerME, TokenizerModel}

import scala.io.Source

object PreProcessor extends LazyLogging {

  val model = new TokenizerModel(new FileInputStream("./models/en-token.bin"))
  val tokenizer = new TokenizerME(model)
  val stopWords: List[String] = loadStopWords()
  val punctuation: List[String] = loadPunctuations()

  def treatData(s: String): String = {
    tokenizer
      .tokenize(s.toLowerCase())
      .filter(x => !stopWords.contains(x))
      .filter(x => !punctuation.contains(x))
      .mkString(" ")
  }

  def loadStopWords(): List[String] = {
    val buff = Source.fromFile("./models/stopwords_en.txt")

    buff
      .getLines()
      .toList
  }

  def loadPunctuations(): List[String] = {
    val buff = Source.fromFile("./models/punctuation_en.txt")

    buff
      .getLines()
      .toList
  }

}
