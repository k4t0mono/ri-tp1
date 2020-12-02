package xyz.stuffium.utils

class RelevantDocument(number: Int, score: String) {

  def number(): Int = this.number

  override def toString = s"<RelevantDocument RN=$number SC=$score />"
}