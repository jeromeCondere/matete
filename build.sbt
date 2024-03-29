import Dependencies._
import  Constants._
import Examples._ 
import SbtProjectImplicits._
import java.util.Properties



lazy val appProperties = settingKey[Properties]("The application properties")


ThisBuild / scalaVersion := appProperties.value.getProperty("scalaVersion")


Global / onChangedBuildSource := ReloadOnSourceChanges






lazy val commonSettings = Seq(
  // organization := "com.matete",
  organization := "io.github.jeromecondere",
  // organizationName := "matete",
  organizationName := "jeromeconderes",
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
  

  developers := List(
    Developer(
      id    = "jeromecondere",
      name  = "Jerome Condere",
      email = "jerome.condere@gmail.com",
      url = url("https://www.google.com/")
    )
  ),
  libraryDependencies += scalaTest % Test,
  libraryDependencies += "org.apache.kafka" % "kafka-streams" % kafkaVersion,
  libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion,
  libraryDependencies += "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  //libraryDependencies += "ch.qos.logback" % "logback-classic" % logback,

  libraryDependencies += "org.apache.logging.log4j" %% "log4j-api-scala" % log4jApiScala,
  libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % log4j2 ,
  libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % log4j2 % Runtime,
  libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % jackson % Runtime,
  libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % jackson % Runtime,
  libraryDependencies += "com.fasterxml.jackson.core" % "jackson-annotations" % jackson % Runtime,
  libraryDependencies += "io.circe" %% "circe-yaml" % circe,
  libraryDependencies += "io.circe" %% "circe-generic" % circe,
  libraryDependencies += "io.circe" %% "circe-generic-extras" % circe,
  libraryDependencies += "io.circe" %% "circe-core" % circe,
  libraryDependencies += "io.circe" %% "circe-parser" % circe,
  libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  libraryDependencies += "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,


  //libraryDependencies += "com.nequissimus" %% "circe-kafka" % "2.7.0",

  libraryDependencies += "io.confluent" % "kafka-avro-serializer" % avroVersion,

  libraryDependencies += "org.nlogo" % "netlogo" % netlogo,

  dependencyOverrides += ("org.jogamp.jogl" % "jogl-all" % jogl),
  dependencyOverrides += ("org.jogamp.gluegen" % "gluegen-rt" % gluegen),
   /*excludeDependencies ++= Seq(
     ExclusionRule("org.parboiled", s"parboiled_$scalaToExclude")
   ),
   libraryDependencies += ("org.parboiled" % "parboiled_2.13" % paraboiled),
   dependencyOverrides += "org.parboiled" % "parboiled_2.13" % paraboiled,
  */
  resolvers ++= Seq( "confluent" at "https://packages.confluent.io/maven/"),
  resolvers ++= Seq("Artifactory" at "https://kaluza.jfrog.io/artifactory/maven"),
  resolvers ++= Seq( "netlogo" at "https://dl.cloudsmith.io/public/netlogo/netlogo/maven/",
      "jogamp" at "https://maven.jzy3d.org/releases/"
  ),
  publishTo := {
    val nexus = "https://s01.oss.sonatype.org/"
    if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishMavenStyle := true,
  versionScheme := Some("early-semver"),
  pomIncludeRepository := { _ => false }

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
  .aggregate(withconfig)
  .aggregate(bank)
  

lazy val pingpong = (project in file("examples/pingpong"))
  .settings(commonSettings)
  .addExampleConfig(pongClass, PongConfig)
  .addExampleConfig(pingClass, PingConfig)
  .addExampleConfig(monitorClass, MonitorConfig)
  .dependsOn(matete)


lazy val covidUtils = (project in file("examples/covid/covidUtils")).settings(
  commonSettings,
  libraryDependencies += "org.postgresql" % "postgresql" % postgresql
).dependsOn(matete)


lazy val covid = (project in file("examples/covid"))
  .settings(commonSettings)
  .addExampleConfig(covidClass, CovidConfig)
  .addExampleConfig(covidHeadlessClass, CovidHeadlessConfig)
  .dependsOn(matete)
  .dependsOn(covidUtils)


lazy val withconfig = (project in file("examples/withconfig"))
  .settings(commonSettings)
  .addExampleConfig(withConfigClass, WithConfig)
  .dependsOn(matete)

lazy val bankUtils = (project in file("examples/bank/bankUtils")).settings(commonSettings).dependsOn(matete)
  
lazy val bank = (project in file("examples/bank"))
  .settings(commonSettings)
  .addExampleConfig(bankClass, BankConfig)
  .addExampleConfig(client1Class, Client1Config)
  .addExampleConfig(client2Class, Client2Config)
  .dependsOn(matete)
  .dependsOn(bankUtils)

