package com.matete.mas.deserialization

import java.io.{ObjectInputStream, ByteArrayInputStream}
import java.util
import org.apache.kafka.common.serialization.Deserializer
import com.matete.mas.agent.AgentMessage

/**
  *  Agent message string deserializer
 **/
class AgentMessageStringDeserializer
    extends Deserializer[AgentMessage[String]] {

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def deserialize(topic: String, bytes: Array[Byte]) = {
    val byteIn = new ByteArrayInputStream(bytes)
    val objIn = new ObjectInputStream(byteIn)
    val obj = objIn.readObject().asInstanceOf[AgentMessage[String]]
    byteIn.close()
    objIn.close()
    obj
  }
  override def close(): Unit = {}

}
