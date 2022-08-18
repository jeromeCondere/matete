import sbt._

lazy val hello = taskKey[Unit]("Prints 'Hello World'")

hello := println("hello world!")


