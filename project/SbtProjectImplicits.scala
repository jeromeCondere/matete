import sbt._
import Keys._
import sbtassembly.AssemblyKeys._
import sbtassembly._
import sbtassembly.AssemblyPlugin.autoImport.baseAssemblySettings


//TODO: fix sources
object SbtProjectImplicits {
    implicit class projectWithexample(x: Project) {
        def addExampleConfig(classname: String, Config: Configuration) = {
            x
            .enablePlugins(sbtassembly.AssemblyPlugin)
            .configs(Config)
            .settings(    
                inConfig(Config)( Defaults.configSettings   ++ baseAssemblySettings ++ Seq(
                sourceDirectory:= baseDirectory.value / "src",
                scalaSource := sourceDirectory.value / "main/scala" / Config.name,
                Compile / sourceDirectory := sourceDirectory.value,
                Compile / scalaSource := scalaSource.value,
                Compile / mainClass := Some(classname),

                mainClass := Some(classname),
                Compile / run / mainClass := Some(classname),
                assembly  := (Config / assembly).value,
                assembly / mainClass := Some(classname),
                assembly / assemblyJarName:= classname.split('.').last + s"-${version.value}_${scalaVersion.value}.jar",
                assembly / assemblyMergeStrategy := {
                    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
                    case x if x.endsWith("module-info.class") => MergeStrategy.discard
                    case x => MergeStrategy.first
                }
            ))  : _*)
            
        }
    }
}
