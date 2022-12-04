package com.matete.mas.experiment

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.matete.mas.configuration.ExperimentConfig
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.apache.logging.log4j.LogManager


trait ExperimentConfigJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport{
  implicit def experimentConfigFormat[E :JsonFormat]: RootJsonFormat[ExperimentConfig[E]] = jsonFormat4(ExperimentConfig.apply[E])
}

class ExperimentServerApi[E: JsonFormat](port: Int = 7070)(implicit system: ActorSystem)  extends ExperimentConfigJsonProtocol{

  implicit val executionContext = system.dispatcher

  final val logger = LogManager.getLogger(s"ExperimentServerApi")


  def descriptionRoute = pathEnd {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Experiment</h1>"))
      }
  }


  def topLevelRoot = concat(
      path("description")(descriptionRoute),
      path("experiment")(experimentRoute)
  )

  val experimentRoute = pathEnd {
      concat(
          get {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Experiment</h1><p>post request to the experiment server</p>"))
          },
          post {

            // decompress gzipped or deflated requests if required
            decodeRequest {
              entity(as[ExperimentConfig[E]]) { experimentConfig =>
                complete {
                  logger.info(s"Starting experiment ${experimentConfig.name}")
                  runExperiment(experimentConfig)
                  "experiment has finished"
                }
              }
            }
          }
      )
  }





  val bindingFuture = Http().newServerAt("localhost", port).bind(topLevelRoot)
  logger.info(s"Server now online. Please navigate to http://localhost:$port/description to get all the info about the api\nPress RETURN to stop...")

 
  
  def runExperiment(experimentConfig: ExperimentConfig[E]) = {

  }

  def stop = {
     bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
  }


}


