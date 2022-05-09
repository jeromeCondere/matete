package com.matete.mas.agent

import scala.util.Try
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._

case class AgentId(id: String)

trait AgentLike[T] {
    /***
     * send message to another agent
     * 
     ***/
    def send(agentId: AgentId, agentMessage: AgentMessage[T])
    def send(agentId: AgentId, agentMessage: String)
    def sendPool(message: String)
    def receive(agentMessage: AgentMessage[T])
    def suicide(): Try[String]
    def broadcast(message: String)
    def forcedie()
    def die()
    def join()
    def disconnect(agentId: AgentId)
    def start(initFunc: Unit)
    def getTopic(agentId: AgentId): String = agentId.id+"-topic"
    
}


abstract class AbstractAgent[T](agentId: AgentId, brokers: List[String])(implicit serializer: Option[String] = None, deserializer: Option[String] = None) extends AgentLike[T] {

    val GENERAL_POOL = "GENERAL_AGENT_POOL"
    val TOPIC = agentId.id + "-topic"
        

    def initProducersProperties: Map[String, Properties] = {
         val  propsString = new Properties()
        propsString.put("bootstrap.servers", brokers.mkString(","))
  
        propsString.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        propsString.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        propsString.put("compression.type", "snappy")

        val  propsAgentMessage = new Properties()
        propsAgentMessage.put("bootstrap.servers", brokers.mkString(","))
  
        propsAgentMessage.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        serializer match {
            case Some(serializerClass) => propsAgentMessage.put("value.serializer", serializerClass)
            case None => propsAgentMessage.put("value.serializer", "my avro") //use avro
        }
        propsAgentMessage.put("value.serializer", serializer)
        propsAgentMessage.put("compression.type", "snappy")

        Map("stringProducerProperties" -> propsString, "generalProducerProperties" -> propsAgentMessage)

    }
    val propsForProducers: Map[String, Properties] =  initProducersProperties

    protected val stringProducer = new KafkaProducer[String, String](propsForProducers("stringProducerProperties"))
    protected val agentMessageProducer = new KafkaProducer[String, AgentMessage[T]](propsForProducers("generalProducerProperties"))   
    

    def initConsumersProperties: Map[String, Properties] = {
        val  propsConsumerString = new Properties()
        propsConsumerString.put("bootstrap.servers", brokers.mkString(","))
  
        propsConsumerString.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        propsConsumerString.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

        val  propsConsumerAgentMessage = new Properties()
        propsConsumerAgentMessage.put("bootstrap.servers", brokers.mkString(","))
  
        propsConsumerAgentMessage.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        deserializer match {
            case Some(deserializerClass) => propsConsumerAgentMessage.put("key.serializer", deserializerClass)
            case None => propsConsumerAgentMessage.put("key.serializer", "my avro") //use avro
        }
        propsConsumerAgentMessage.put("value.serializer", serializer)

        Map("stringConsumerProperties" -> propsConsumerString, "generaConsumerProperties" -> propsConsumerAgentMessage)
 }    

    override  def send(agentId: AgentId, agentMessage: AgentMessage[T]) = {
        val record = new ProducerRecord(getTopic(agentId), agentId.id, agentMessage)

        agentMessageProducer.send(record)
    }


   
    override  def send(agentId: AgentId, agentMessage: String) = {
        val record = new ProducerRecord(getTopic(agentId), agentId.id, agentMessage)
        stringProducer.send(record)
    }


    override def broadcast(message: String): Unit = {
                val record = new ProducerRecord(GENERAL_POOL, agentId.id, message)
        stringProducer.send(record)
        
    }

     

}

