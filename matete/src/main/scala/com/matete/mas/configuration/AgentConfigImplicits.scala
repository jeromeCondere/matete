package com.matete.mas.configuration
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.auto._
import io.circe.syntax._

object AgentConfigImplicits {

    implicit val encoderAgentConfig: Encoder[AgentConfig] =
    Encoder.forProduct4("description", "id","producers", "consumers")(x =>AgentConfig.unapply(x).get)
    implicit val encoderConsumerConfig: Encoder[ConsumerConfig] =
    Encoder.forProduct7(
    "name",
    "description", 
    "keyDeserializer",
    "valueDeserializer", 
    "autoCommit",
    "groupId",
    "additionalParameters")(ConsumerConfig.unapply(_).get)

    implicit val encoderProducerConfig: Encoder[ProducerConfig] = 
    Encoder.forProduct6(
    "name",
    "description", 
    "keySerializer",
    "valueSerializer", 
    "compressionType",
    "additionalParameters")(ProducerConfig.unapply(_).get)

    implicit val encoderParameter: Encoder[Parameter] =
    Encoder.forProduct2("key", "value")(Parameter.unapply(_).get)

      // implicit val encoderOptionalAmount: Encoder[Option[Amount]] = (optA: Option[Amount]) =>
      // optA match {
      //   case Some(amount) => amount.asJson
      //   case None => Json.obj("waived" -> true.asJson)
      // }

    // decoder

    implicit val decoderAgentConfig: Decoder[AgentConfig] =
    Decoder.forProduct4("description", "id","producers", "consumers")(AgentConfig.apply)
    implicit val decoderConsumerConfig: Decoder[ConsumerConfig] =
    Decoder.forProduct7(
    "name",
    "description", 
    "keyDeserializer",
    "valueDeserializer", 
    "autoCommit",
    "groupId",
    "additionalParameters")(ConsumerConfig.apply)

    implicit val decoderProducerConfig: Decoder[ProducerConfig] =
    Decoder.forProduct6(
    "name",
    "description", 
    "keySerializer",
    "valueSerializer", 
    "compressionType",
    "additionalParameters")(ProducerConfig.apply)

    implicit val decoderParameter: Decoder[Parameter] =
    Decoder.forProduct2("key", "value")(Parameter.apply)
}