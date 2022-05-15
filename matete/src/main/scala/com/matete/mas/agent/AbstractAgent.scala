package com.matete.mas.agent

import scala.util.Try
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util
import scala.collection.JavaConverters._

case class AgentId(id: String)

trait AgentLike[T] {
    /***
     * send message to another agent
     * 
     ***/
    def init: Unit = {}
    def send(agentIdReceiver: AgentId, message: T)
    def send(agentId: AgentId, agentMessage: String)
    def sendPool(message: String)
    def receive(agentMessages: List[AgentMessage[T]])
    def receiveSimpleMessages(agentMessages: List[String])

    def suicide: Unit
    def broadcast(message: String)
    def forcedie: Unit
    def die: Unit
    def join(agentId: AgentId)
    def join(agentIds: List[AgentId])
    def disconnect(agentId: AgentId)
    def run
    def pollingLoop
    def getTopic(agentId: AgentId): String = agentId.id+"-topic"
    def getTopicGroupBase(agentId: AgentId): String = getTopic(agentId)+"-group"
    
}


abstract class AbstractAgent[T](agentId: AgentId, brokers: List[String])
(initProducerProperties: Map[String, Properties] => Map[String, Properties] = id => id,  
    initConsumersProperties:  Map[String, Properties] => Map[String, Properties] = id => id)
(implicit serializer: Option[String] = None, deserializer: Option[String] = None) extends AgentLike[T] {

    val GENERAL_POOL: String = "GENERAL_AGENT_POOL"
    val TOPIC: String = getTopic(agentId)
    protected var wantToDie: Boolean = false
    protected val stringKeySuffix = "-str"
     //TODO: add list of agent ids that are connected to the agent

    def initDefaultProducersProperties: Map[String, Properties] = {
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
            case None => propsAgentMessage.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer") //use avro
        }
        propsAgentMessage.put("compression.type", "snappy")

        Map("stringProducerProperties" -> propsString, "agentMessageProducerProperties" -> propsAgentMessage)

    }
    val propsForProducers: Map[String, Properties] =  initProducerProperties(initDefaultProducersProperties)

 

    protected def initDefaultConsumersProperties: Map[String, Properties] = {
        val  propsConsumerString = new Properties()
        propsConsumerString.put("bootstrap.servers", brokers.mkString(","))
  
        propsConsumerString.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        propsConsumerString.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        propsConsumerString.put("group.id", getTopicGroupBase(agentId)+"-classic")
        propsConsumerString.put("enable.auto.commit", "false")


        val  propsConsumerAgentMessage = new Properties()
        propsConsumerAgentMessage.put("bootstrap.servers", brokers.mkString(","))
  
        propsConsumerAgentMessage.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        deserializer match {
            case Some(deserializerClass) => propsConsumerAgentMessage.put("value.deserializer", deserializerClass)
            case None => propsConsumerAgentMessage.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer") //use avro
        }
        propsConsumerAgentMessage.put("group.id", getTopicGroupBase(agentId)+"-agent")
        propsConsumerAgentMessage.put("enable.auto.commit", "false")


        Map("stringConsumerProperties" -> propsConsumerString, "agentMessageConsumerProperties" -> propsConsumerAgentMessage)
    }  
  
    val propsForConsumer: Map[String, Properties] = initConsumersProperties(initDefaultConsumersProperties)



    protected val stringProducer = new KafkaProducer[String, String](propsForProducers("stringProducerProperties"))
    protected val agentMessageProducer = new KafkaProducer[String, AgentMessage[T]](propsForProducers("agentMessageProducerProperties"))   

    protected val stringConsumer = new KafkaConsumer[String, String](propsForConsumer("stringConsumerProperties"))
    protected val agentMessageConsumer = new KafkaConsumer[String, AgentMessage[T]](propsForConsumer("agentMessageConsumerProperties")) 

    this.agentMessageConsumer.subscribe(util.Collections.singletonList(TOPIC))
    this.stringConsumer.subscribe(util.Collections.singletonList(TOPIC))

    override def join(agentId: AgentId) = {
        this.agentMessageConsumer.subscribe(util.Collections.singletonList(getTopic(agentId)))
    
    } 

    override def join(agentIds: List[AgentId]) = {
        this.agentMessageConsumer.subscribe(agentIds.map(getTopic).asJava)
    }  

    override  def send(agentIdReceiver: AgentId, message: T) = {
        // get topic from receiver and then  send it
        val record = new ProducerRecord(getTopic(agentIdReceiver), s"${agentIdReceiver.id}", AgentMessage(this.agentId, message))
        this.agentMessageProducer.send(record)
    }


   
    override  def send(agentIdReceiver: AgentId, agentMessage: String) = {
        val record = new ProducerRecord(getTopic(agentIdReceiver), s"from_${this.agentId.id}_to_${agentIdReceiver.id}${this.stringKeySuffix}", agentMessage)
        this.stringProducer.send(record)
    }


    override def broadcast(message: String): Unit = {
        val record = new ProducerRecord(this.GENERAL_POOL, agentId.id, message)
        this.stringProducer.send(record)
        
    }

    override def die: Unit = {
      this.agentMessageConsumer.close
      this.stringConsumer.close
      this.agentMessageProducer.close
      this.stringProducer.close
    }

    def forcedie: Unit = this.wantToDie = true


     

}