name := """play-image-api"""

organization := "ch.wavein"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14",
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.5",
  "net.kaliber" %% "play-s3" % "8.0.0",
  "net.codingwell" %% "scala-guice" % "4.1.0"
)
