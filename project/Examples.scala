import  sbt._

object Examples {
  lazy val pingpongClass = "com.matete.examples.PingPongApp"
  lazy val pongClass = "com.matete.examples.PongApp"
  lazy val pingClass = "com.matete.examples.PingApp"


  lazy val PongConfig = config("pong").describedAs("pong config") extend(Compile, Runtime, Test)
  lazy val PingConfig = config("ping").describedAs("Dependencies required in all configurations.") extend(Compile, Runtime, Test)

  

}
