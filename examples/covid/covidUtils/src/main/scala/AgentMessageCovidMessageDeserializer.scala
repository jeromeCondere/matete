package com.matete.examples.covid

import java.io.{ObjectInputStream, ByteArrayInputStream}
import java.util
import org.apache.kafka.common.serialization.Deserializer
import com.matete.mas.agent.AgentMessage
import CovidTurtleCodecsImplicits._
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Error, Json}
import java.nio.charset.StandardCharsets

class AgentMessageCovidMessageDeserializer extends Deserializer[AgentMessage[CovidMessage]]{

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}  

  override def deserialize(topic:String,bytes: Array[Byte]) = {
    (for {
          json <- parse(new String(bytes, StandardCharsets.UTF_8)): Either[Error, Json]
          t <- json.as[AgentMessage[CovidMessage]]: Either[Error, AgentMessage[CovidMessage]]
        } yield
          t
      ).fold(error => throw new RuntimeException(s"Deserialization failure: ${error.getMessage}", error), identity _)
  }
  override def close():Unit = {

  }
}
