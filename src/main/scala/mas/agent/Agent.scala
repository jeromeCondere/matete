package mas.agent

import scala.util.Try
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer

case class AgentId(id: String)

trait AgentLike[T] {
    /***
     * send message to another agent
     * 
     ***/
    def send(agentId: AgentId, agentMessage: AgentMessage[T])
    def sendPool(message: String)
    def receive(agentMessage: AgentMessage[T])
    def suicide(): Try[String]
    def broadcast()
    def forcedie()
    def join()
    def disconnect(agentId: AgentId)
    def start(initFunc: Unit)
}


abstract class AbstractAgent[T](id: AgentId, brokers: List[String])(implicit serializer: Option[String] , deserializer: Option[String]) extends AgentLike[T] {
    val propsForProducers: Map[String, Properties] = ???

    def initProducersProperties: Map[String, Properties] = {
         val  propsString = new Properties()
        propsString.put("bootstrap.servers", brokers.mkString(","))
  
        propsString.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        propsString.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        propsString.put("compression.type", "snappy")

        val  propsMessage = new Properties()
        propsMessage.put("bootstrap.servers", brokers.mkString(","))
  
        propsMessage.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        serializer match {
            case Some(serializerClass) => propsMessage.put("key.serializer", serializerClass)
            case None => propsMessage.put("key.serializer", "my avro") //use avro
        }
        propsMessage.put("value.serializer", serializer)
        propsMessage.put("compression.type", "snappy")

        Map("StringProp" -> propsString, "GeneralProp" -> propsMessage)

    }

     

}

