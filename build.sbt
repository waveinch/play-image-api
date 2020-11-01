name := """play-image-api"""

organization := "ch.wavein"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.3"

resolvers += Resolver.bintrayRepo("waveinch","maven")

libraryDependencies ++= Seq(
  ws,
  guice,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.18.1-play27",
  "com.sksamuel.scrimage" % "scrimage-core" % "4.0.10",
  "net.kaliber" %% "play-s3" % "11.0.0",
  "net.codingwell" %% "scala-guice" % "4.2.11"
)
