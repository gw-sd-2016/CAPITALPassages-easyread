name := """EasyRead"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  ws,
  javaWs,


"org.apache.directory.studio" % "org.apache.commons.io" % "2.4",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.4.1",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.4.1" classifier "models",
  "net.sf.extjwnl" % "extjwnl" % "1.9",
  "net.sf.extjwnl" % "extjwnl-data-wn31" % "1.2"
)

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
