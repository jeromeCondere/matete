import  sbt._

object Examples {
  lazy val pingpongClass = "com.matete.examples.pingpong.PingPongApp"
  lazy val pongClass = "com.matete.examples.pingpong.PongApp"
  lazy val pingClass = "com.matete.examples.pingpong.PingApp"


  lazy val PongConfig = config("Pong").describedAs("pong config") extend(Compile, Test)
  lazy val PingConfig = config("Ping").describedAs("ping config") extend(Compile, Test)

}
