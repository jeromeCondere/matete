import Dependencies._
import  Constants._
import Examples._ 
import SbtProjectImplicits._
import java.util.Properties

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val appProperties = settingKey[Properties]("The application properties")


ThisBuild / scalaVersion := appProperties.value.getProperty("scalaVersion")
//val setLog4jDebug = sys.props("log4j2.debug") = "true"


lazy val commonSettings = Seq(
  organization := "com.matete",
  appProperties := {
      val prop = new Properties()
      try {
          
      val file = new File("project/build.properties")
      IO.load(prop, file)
      
      } catch {
        case e:Exception => println(e)
      }

      prop
    },
  version := appProperties.value.getProperty("version"),
  scalaVersion := appProperties.value.getProperty("scalaVersion"),
  organizationName := "matete",
  libraryDependencies += scalaTest % Test,
  libraryDependencies += "org.apache.kafka" % "kafka-streams" % kafkaVersion,
  libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion,
  libraryDependencies += "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  libraryDependencies += "io.confluent" % "kafka-avro-serializer" % avroVersion,
  //libraryDependencies += "ch.qos.logback" % "logback-classic" % logback,

  libraryDependencies += "org.apache.logging.log4j" %% "log4j-api-scala" % log4jApiScala,
  libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % log4j2 ,
  libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % log4j2 % Runtime,
  libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % jackson % Runtime,
  libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % jackson % Runtime,
  libraryDependencies += "com.fasterxml.jackson.core" % "jackson-annotations" % jackson % Runtime,
  resolvers ++= Seq( "confluent" at "https://packages.confluent.io/maven/")

)

lazy val core = (project in file(".")).settings(
  commonSettings
).aggregate(matete, examples)

lazy val matete = (project in file("matete"))
  .settings(
    commonSettings,
    name := "matete-mas",

  )

lazy val examples = (project in file("examples"))
  .settings(
    commonSettings,
      name := "matete-examples"
  )
  .aggregate(pingpong)
  

  lazy val pingpong = (project in file("examples/pingpong"))
   .settings(commonSettings)
   .addExampleConfig(pongClass, PongConfig)
   .addExampleConfig(pingClass, PingConfig)
   .dependsOn(matete)
