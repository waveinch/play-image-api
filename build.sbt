name := """play-image-api"""

organization := "ch.wavein"


lazy val root = (project in file("."))
  .settings(
    bintrayRepository := "maven",
    bintrayOrganization := Some("waveinch"),
    publishMavenStyle := true,
    licenses += ("Apache-2.0", url("http://www.opensource.org/licenses/apache2.0.php")),
    git.useGitDescribe := true
  )
  .enablePlugins(
    PlayScala,
    GitVersioning
  )

scalaVersion := "2.13.3"

resolvers += Resolver.bintrayRepo("waveinch","maven")

libraryDependencies ++= Seq(
  ws,
  guice,
  "org.reactivemongo" %% "play2-reactivemongo" % "1.0.0-play27",
  "com.sksamuel.scrimage" % "scrimage-core" % "4.0.10",
  "net.kaliber" %% "play-s3" % "11.0.0",
  "net.codingwell" %% "scala-guice" % "4.2.11"
)
