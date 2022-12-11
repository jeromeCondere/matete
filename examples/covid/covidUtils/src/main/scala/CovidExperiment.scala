package com.matete.examples.covid

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport


case class CovidExperimentParameter(recorveryChanceEpsilon: Double, infectionChanceEpsilon: Double, repeat: Int)


object CovidExperimentParameterJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport{
  implicit val covidParamFormat = jsonFormat3(CovidExperimentParameter.apply)
}