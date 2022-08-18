import  sbt._

object Examples {

  lazy val pingpongClass = "com.matete.examples.pingpong.PingPongApp"
  lazy val pongClass = "com.matete.examples.pingpong.PongApp"
  lazy val pingClass = "com.matete.examples.pingpong.PingApp"
  lazy val monitorClass = "com.matete.examples.pingpong.MonitorApp"

  lazy val withConfigClass = "com.matete.examples.withconfig.WithConfigApp"

  lazy val bankClass = "com.matete.examples.bank.BankApp"
  lazy val client1Class = "com.matete.examples.bank.Client1App"
  lazy val client2Class = "com.matete.examples.bank.Client2App"

  lazy val covidClass = "com.matete.examples.covid.CovidApp"
  lazy val covidHeadlessClass = "com.matete.examples.covid.CovidHeadlessApp"



  lazy val PongConfig = config("Pong").describedAs("pong config") extend(Compile, Test)
  lazy val PingConfig = config("Ping").describedAs("ping config") extend(Compile, Test)
  lazy val MonitorConfig = config("Monitor").describedAs("ping config") extend(Compile, Test)

  lazy val WithConfig = config("WithConfig").describedAs("with config") extend(Compile, Test)


  //lazy val BankUtilsConfig = config("utils").describedAs("bank utils config") extend(Compile, Test)
  lazy val BankConfig = config("Bank").describedAs("bank config") extend(Compile, Test)
  lazy val Client1Config = config("Client1").describedAs("bank config") extend(Compile, Test)
  lazy val Client2Config = config("Client2").describedAs("bank config") extend(Compile, Test)



  lazy val CovidConfig = config("Covid").describedAs("covid config") extend(Compile, Test)
  lazy val CovidHeadlessConfig = config("CovidHeadless").describedAs("covid headless config") extend(Compile, Test)


}
