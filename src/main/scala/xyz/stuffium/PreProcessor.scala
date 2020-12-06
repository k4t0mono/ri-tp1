package xyz.stuffium

import java.io.FileInputStream

import com.typesafe.scalalogging.LazyLogging
import opennlp.tools.tokenize.{TokenizerME, TokenizerModel}

import scala.io.Source

object PreProcessor extends LazyLogging {

  val model = new TokenizerModel(new FileInputStream("./models/en-token.bin"))
  val tokenizer = new TokenizerME(model)
  val stopWords: List[String] = loadStopWords()
  val punctuation: List[Char] = loadPunctuations()

  def treatData(_s: String): String = {
    val s = _s
      .toLowerCase()
      .split(" ")
      .map(x => x.toList)
      .filter(x => x.nonEmpty)
      .map(x => x.filter(x => !punctuation.contains(x)).mkString(""))
      .mkString(" ")

    tokenizer
      .tokenize(s)
      .filter(x => !stopWords.contains(x))
      .mkString(" ")
  }

  def loadStopWords(): List[String] = {
    val buff = Source.fromFile("./models/stopwords_en.txt")

    buff
      .getLines()
      .toList
  }

  def loadPunctuations(): List[Char] = {
    val buff = Source.fromFile("./models/punctuation_en.txt")

    buff
      .getLines()
      .toList
      .map(x => x.toCharArray.head)
  }

}
