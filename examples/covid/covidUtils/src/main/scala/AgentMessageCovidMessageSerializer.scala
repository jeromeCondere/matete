package com.matete.examples.covid

import java.io.{ObjectOutputStream, ByteArrayOutputStream}
import java.util
import org.apache.kafka.common.serialization.Serializer
import com.matete.mas.agent.AgentMessage
import io.circe.syntax._
import CovidTurtleCodecsImplicits._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Error, Json}
import java.nio.charset.StandardCharsets

class AgentMessageCovidMessageSerializer extends Serializer[AgentMessage[CovidMessage]]{

  override def configure(configs: util.Map[String,_],isKey: Boolean):Unit = {

  }


  override def serialize(topic:String, data: AgentMessage[CovidMessage]):Array[Byte] = {
    try {
      data.asJson.noSpaces.getBytes(StandardCharsets.UTF_8)
    }
    catch {
      case ex: Exception => throw new Exception(ex.getMessage)
    }
  }

  override def close():Unit = {

  }


}
