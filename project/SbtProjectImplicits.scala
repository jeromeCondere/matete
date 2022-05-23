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
            .settings(    
                inConfig(Config)( Defaults.configSettings ++ Defaults.compileSettings  ++ baseAssemblySettings ++ Seq(
                sourceDirectory:= sourceDirectory.value / "../main/",  
                // Compile / compile := (Config / Compile / compile).value,              
                Compile / compile / mainClass := Some(classname),
                //Compile /  compile /sources ~= (_.filter(_.name == classname.split('.').last + ".scala")),
                 mainClass := Some(classname),
                Compile / run / mainClass := Some(classname),
                //run / sources ~= (_.filter(_.name == classname.split('.').last + ".scala")),
                assembly  := (Config / assembly).value,
                assembly / mainClass := Some(classname),
                assembly / assemblyMergeStrategy := {
                    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
                    case x if x.endsWith("module-info.class") => MergeStrategy.discard
                    case x => MergeStrategy.first
                    
                }
            )) : _*)
            
        }
    }
}
