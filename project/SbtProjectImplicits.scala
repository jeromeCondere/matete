import sbt._
import Keys._
import sbtassembly.AssemblyKeys._
import sbtassembly._

object SbtProjectImplicits {
    implicit class projectWithexample(x: Project) {
        def addExampleConfig(classname: String, Config: Configuration) = {
            x
            .enablePlugins(sbtassembly.AssemblyPlugin)
            .settings(    
                inConfig(Config)(Defaults.testTasks ++ Seq(
                mainClass in Compile := Some(classname),
                run / mainClass:= Some(classname),
                assembly / mainClass:= Some(classname),
                assembly / assemblyMergeStrategy := {
                case PathList("META-INF", xs @ _*) => MergeStrategy.discard
                case x => MergeStrategy.first
                }
            )) : _*)
            
        }
    }
}