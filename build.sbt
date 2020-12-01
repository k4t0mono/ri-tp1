name := "ri-tp1"

version := "0.1"

scalaVersion := "2.13.4"

libraryDependencies ++= Seq(
  // Logging
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",

  "org.apache.lucene" % "lucene-core" % "8.7.0",
  "org.apache.lucene" % "lucene-queryparser" % "8.7.0",
)