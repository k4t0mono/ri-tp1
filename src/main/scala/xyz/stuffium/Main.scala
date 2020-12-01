package xyz.stuffium

import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging {

  org.slf4j.LoggerFactory
    .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[ch.qos.logback.classic.Logger]
    .setLevel(ch.qos.logback.classic.Level.TRACE)

  def main(args: Array[String]): Unit = {
    logger.info("Warp 10, engage")

    logger.info("Say goodbye Data")
  }

}
