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
                //Compile / compile := (Config /compile).value,

 
                // Compile / compile := (Config / Compile / compile).value,              
                // Compile / compile / mainClass := Some(classname),
                // Compile  / sources := (Compile /  sources).map(_.filter(_.name == classname.split('.').last + ".scala")).value,
                 mainClass := Some(classname),
                Compile / run / mainClass := Some(classname),
                //run / sources ~= (_.filter(_.name == classname.split('.').last + ".scala")),
                assembly  := (Config / assembly).value,
                assembly / mainClass := Some(classname),
                assembly / assemblyJarName:= classname.split('.').last + s"-${version.value}_${scalaVersion.value}",
                assembly / assemblyMergeStrategy := {
                    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
                    case x if x.endsWith("module-info.class") => MergeStrategy.discard
                    case x => MergeStrategy.first
                    
                }
            ))  : _*)
            
        }
    }
}
