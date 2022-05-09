import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.matete.mas"
ThisBuild / organizationName := "matete"

lazy val root = (project in file("."))
  .settings(
    name := "matete",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.apache.kafka" % "kafka-streams" % "3.1.0",
    libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "3.1.0",
    libraryDependencies += "org.apache.kafka" % "kafka-clients" % "3.1.0",
    libraryDependencies += "io.confluent" % "kafka-avro-serializer" % "7.1.1",
    resolvers ++= Seq( "confluent" at "https://packages.confluent.io/maven/")
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
