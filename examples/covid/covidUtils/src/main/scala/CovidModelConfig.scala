package com.matete.examples.covid

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto

// p transmission: among the people travelling how much are going to the country
case class Frontier(countryId: String, pTransmission: Float)
case class CovidModelConfig(
    name: String, initialPeople: Int,
    pTravel: Double,
    infectionChance: Double,
    recoveryChance: Double,
    averageRecoveryTime: Int,
    frontiers: List[Frontier]
)
case class GlobalModelConfig(models: List[CovidModelConfig])


object CovidConfigImplicits {
    implicit val customConfig: Configuration = Configuration.default.withDefaults

    // encoder
    implicit val encoderFrontier: Encoder[Frontier] =  deriveEncoder[Frontier]
    implicit val encoderCovidConfig: Encoder[CovidModelConfig] =  deriveEncoder[CovidModelConfig]
    implicit val encoderGlobalCovidConfig: Encoder[GlobalModelConfig] = deriveEncoder[GlobalModelConfig]


    // decoder
    implicit val decoderFrontier: Decoder[Frontier] = semiauto.deriveConfiguredDecoder[Frontier]
    implicit val decoderCovidConfig: Decoder[CovidModelConfig] = semiauto.deriveConfiguredDecoder[CovidModelConfig]
    implicit val decoderGlobalCovidConfig: Decoder[GlobalModelConfig] = semiauto.deriveConfiguredDecoder[GlobalModelConfig]

}