package com.matete.mas.configuration
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto

object AgentConfigImplicits {
    implicit val customConfig: Configuration = Configuration.default.withDefaults

    // encoder

    implicit val encoderAgentConfig: Encoder[AgentConfig] =  deriveEncoder[AgentConfig]
    implicit val encoderConsumerConfig: Encoder[ConsumerConfig] = deriveEncoder[ConsumerConfig]
    implicit val encoderProducerConfig: Encoder[ProducerConfig] =  deriveEncoder[ProducerConfig]
    implicit val encoderParameter: Encoder[Parameter] = deriveEncoder[Parameter]


    // decoder

    implicit val decoderAgentConfig: Decoder[AgentConfig] = semiauto.deriveConfiguredDecoder[AgentConfig]
    implicit val decoderConsumerConfig: Decoder[ConsumerConfig] = semiauto.deriveConfiguredDecoder[ConsumerConfig]
    implicit val decoderProducerConfig: Decoder[ProducerConfig] = semiauto.deriveConfiguredDecoder[ProducerConfig]
    implicit val decoderParameter: Decoder[Parameter] = semiauto.deriveConfiguredDecoder[Parameter]

}