package com.matete.examples.covid

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto
import com.matete.mas.agent.AgentId
import com.matete.mas.agent.AgentMessage


object CovidTurtleCodecsImplicits {
    implicit val customConfig: Configuration = Configuration.default.withDefaults

    // encoder
    implicit val encoderCovidTurtle: Encoder[CovidTurtle] =  deriveEncoder[CovidTurtle]
    implicit val encoderAgentId: Encoder[AgentId] =  deriveEncoder[AgentId]
    implicit val encoderCovidModelBehaviour: Encoder[CovidModelBehaviour] =  deriveEncoder[CovidModelBehaviour]
    implicit val encoderCovidMessage: Encoder[CovidMessage] =  deriveEncoder[CovidMessage]
    implicit val encoderCovidTurtleMessage: Encoder[AgentMessage[CovidMessage]] = deriveEncoder[AgentMessage[CovidMessage]]


    // decoder
    implicit val decoderCovidTurtle: Decoder[CovidTurtle] = semiauto.deriveConfiguredDecoder[CovidTurtle]
    implicit val decoderAgentId: Decoder[AgentId] = semiauto.deriveConfiguredDecoder[AgentId]
    implicit val decoderCovidModelBehaviour: Decoder[CovidModelBehaviour] = semiauto.deriveConfiguredDecoder[CovidModelBehaviour]
    implicit val decoderCovidMessage: Decoder[CovidMessage] = semiauto.deriveConfiguredDecoder[CovidMessage]
    implicit val decoderCovidTurtleMessage: Decoder[AgentMessage[CovidMessage]] = semiauto.deriveConfiguredDecoder[AgentMessage[CovidMessage]]

}