package com.matete.mas.configuration

import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._


case class ExperimentConfig[T](
    id: String,
    name: String,
    description: Option[String] = None,
    parameters: T
)


