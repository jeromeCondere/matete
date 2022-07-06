import  sbt._

object Examples {
  lazy val pingpongClass = "com.matete.examples.pingpong.PingPongApp"
  lazy val pongClass = "com.matete.examples.pingpong.PongApp"
  lazy val pingClass = "com.matete.examples.pingpong.PingApp"
  lazy val monitorClass = "com.matete.examples.pingpong.MonitorApp"
  lazy val withConfigClass = "com.matete.examples.withconfig.WithConfigApp"
  lazy val bankClass = "com.matete.examples.bank.BankApp"


  lazy val PongConfig = config("Pong").describedAs("pong config") extend(Compile, Test)
  lazy val PingConfig = config("Ping").describedAs("ping config") extend(Compile, Test)
  lazy val MonitorConfig = config("Monitor").describedAs("ping config") extend(Compile, Test)
  lazy val WithConfig = config("WithConfig").describedAs("with config") extend(Compile, Test)
  lazy val BankConfig = config("Bank").describedAs("bank config") extend(Compile, Test)

}
