// Your profile name of the sonatype account. The default is the same with the organization value
sonatypeProfileName := "org.xerial"

// To sync with Maven central, you need to supply the following information:
pomExtra in Global := {
  <url>https://github.com/waveinch/play-image-api</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:github.com/waveinch/play-image-api</connection>
      <developerConnection>scm:git:git@github.com/waveinch/play-image-api</developerConnection>
      <url>https://github.com/waveinch/play-image-api</url>
    </scm>
    <developers>
      <developer>
        <id>minettiandrea</id>
        <name>Andrea Minetti</name>
        <url>http://wavein.ch</url>
      </developer>
    </developers>
}