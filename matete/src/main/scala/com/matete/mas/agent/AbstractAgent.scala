package com.matete.mas.agent

import scala.util.Try
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util
import scala.collection.JavaConverters._
import org.apache.logging.log4j.LogManager
import com.matete.mas.configuration._
import org.apache.avro.Schema
import org.apache.avro.Schema.Parser

case class AgentId(id: String)

/**
  * 
  *
  * @param configuration agent configuration
  * @param defaultSerializer agent message serializer
  * @param defaultDeserializer agent message deserializer
  * 
  * @todo change to H <: AgentConfig
  */ 
abstract class AbstractAgent[T](configuration: AgentConfig)( defaultSerializer: Option[String] = None, defaultDeserializer: Option[String] = None) extends AgentLike[T] {

    val agentId: AgentId = AgentId(configuration.id)
    val brokers = configuration.brokers
    val GENERAL_POOL: String = "GENERAL_AGENT_POOL"
    val TOPIC: String = getTopic(AgentId(configuration.id))
    protected var wantToDie: Boolean = false
    protected val stringKeySuffix = "-str"
    final val logger = LogManager.getLogger(s"Agent - ${agentId.id}")



    def initDefaultProducersProperties: Map[String, Properties] = {

        val  propsAgentMessage = new Properties()
        propsAgentMessage.put("bootstrap.servers", brokers.mkString(","))
  
        propsAgentMessage.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        defaultSerializer match {
            case Some(serializerClass) => propsAgentMessage.put("value.serializer", serializerClass)
            case None => propsAgentMessage.put("value.serializer", "com.matete.mas.serialization.AgentMessageStringSerializer") //use avro
        }
        propsAgentMessage.put("compression.type", "snappy")

        Map("defaultAgentMessageProducer" -> propsAgentMessage)

    }

 

    def initDefaultConsumersProperties: Map[String, Properties] = {

        val  propsConsumerAgentMessage = new Properties()
        propsConsumerAgentMessage.put("bootstrap.servers", brokers.mkString(","))
        propsConsumerAgentMessage.put("group.id", getTopicGroupBase(agentId)+"-classic")
        propsConsumerAgentMessage.put("enable.auto.commit", "false")
  
        propsConsumerAgentMessage.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        defaultDeserializer match {
            case Some(deserializerClass) => propsConsumerAgentMessage.put("value.deserializer", deserializerClass)
            case None => propsConsumerAgentMessage.put("value.deserializer", "com.matete.mas.deserialization.AgentMessageStringDeserializer") //use avro
        }
        propsConsumerAgentMessage.put("group.id", getTopicGroupBase(agentId)+"-agent")
        propsConsumerAgentMessage.put("enable.auto.commit", "false")


        Map("defaultAgentMessageConsumer" -> propsConsumerAgentMessage)
    } 


    def initProducersProperties: Map[String, Properties] = {
        initDefaultProducersProperties ++ (configuration.producers match {
        
            case Some(producersList) => producersList.foldLeft(Map.empty[String, Properties]){
                    case(mapProp, producerConfig) =>    val  props = new Properties()
                        props.put("bootstrap.servers", brokers.mkString(","))
                
                        props.put("key.serializer", producerConfig.keySerializer)
                        props.put("value.serializer", producerConfig.valueSerializer)
                        props.put("compression.type", producerConfig.compressionType)
                        producerConfig.additionalParameters.toList.flatten.foreach{
                            case Parameter(k,v) => props.put(k,v)
                        
                        }
                        mapProp + (producerConfig.name ->  props)
                }
            case None => Map.empty[String, Properties]

        })
    }

    def initConsumerProperties: Map[String, Properties] = {
        initDefaultConsumersProperties ++ (configuration.consumers match {
        
            case Some(consumerList) => consumerList.foldLeft(Map.empty[String, Properties]){
                    case(mapProp, consumerConfig) =>    val  props = new Properties()
                        props.put("bootstrap.servers", brokers.mkString(","))
                
                        props.put("key.deserializer", consumerConfig.keyDeserializer)
                        props.put("value.deserializer", consumerConfig.valueDeserializer)
                        props.put("group.id", consumerConfig.groupId)
                        props.put("enable.auto.commit", consumerConfig.autoCommit)
                        consumerConfig.additionalParameters.toList.flatten.foreach{
                            case Parameter(k,v) => props.put(k,v)
                        
                        }
                        mapProp + (consumerConfig.name ->  props)
                }
            case None => Map.empty[String, Properties]
        })
    }

    val propsForProducers: Map[String, Properties] =  initProducersProperties
    val propsForConsumers: Map[String, Properties] = initConsumerProperties



    // take only the agent message producers
    protected val producers: Map[String, KafkaProducer[String, AgentMessage[T]]] = propsForProducers.map{
        case (name,props) => (name, new KafkaProducer[String, AgentMessage[T]](props) )
    }

    protected val agentMessageProducer = producers("defaultAgentMessageProducer")  


    // take only the agent message consumers
    protected val consumers: Map[String, KafkaConsumer[String, AgentMessage[T]]] = propsForConsumers.map{
        case (name,props) => (name, new KafkaConsumer[String, AgentMessage[T]](props) )
    }
    protected val agentMessageConsumer = consumers("defaultAgentMessageConsumer")


    // subscribe to the agent topic
    consumers.foreach{
        case(_, kafkaConsumer) => kafkaConsumer.subscribe(util.Collections.singletonList(TOPIC))
    }


    override def join(agentId: AgentId) = {
        this.agentMessageConsumer.subscribe(util.Collections.singletonList(getTopic(agentId)))
    
    } 

    override def join(agentIds: List[AgentId]) = {
        this.agentMessageConsumer.subscribe(agentIds.map(getTopic).asJava)
    }  

    /***
     * send message to agentIdReceiver
     *  @param agentIdReceiver  agent that the message is sent to
     *  @param message message sent to agentIdReceiver
     * 
     ***/
    override  def send(agentIdReceiver: AgentId, message: T) = {
        // get topic from receiver and then  send it
        val record = new ProducerRecord(getTopic(agentIdReceiver), s"${agentIdReceiver.id}", AgentMessage(this.agentId, message))
        this.agentMessageProducer.send(record)
    }




    /***
     * close all the consumers and producers
     ***/
    override def die: Unit = {
        
      this.consumers.foreach{ case(_, kafkaConsumer) => kafkaConsumer.close }
      this.producers.foreach{ case(_, kafkaProducer) => kafkaProducer.close }

    }

    def forcedie: Unit = this.wantToDie = true

}