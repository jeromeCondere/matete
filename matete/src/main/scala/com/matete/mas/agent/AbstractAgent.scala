package com.matete.mas.agent

import java.util.Properties
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import java.util
import scala.collection.JavaConverters._
import org.apache.logging.log4j.LogManager
import com.matete.mas.configuration._


case class AgentId(id: String)

/**
  * 
  * @param configuration agent configuration
  * @param defaultSerializer serializer used to send the message of type T.
  * @param defaultDeserializer serializer used to receive the message of type T.
  * 
  */ 
abstract class AbstractAgent[T](configuration: AgentConfig)( defaultSerializer: Option[String] = None, defaultDeserializer: Option[String] = None) extends AgentLike[T] {

    val agentId: AgentId = AgentId(configuration.id)
    val brokers = configuration.brokers

    def GENERAL_POOL: String = "GENERAL_AGENT_POOL"
    /**
     * TOPIC = agentId + "-topic"
     **/
    def TOPIC: String = getTopic(AgentId(configuration.id))

    val description = configuration.description
    protected var wantToDie: Boolean = false
    protected val stringKeySuffix = "-str"
    final val logger = LogManager.getLogger(s"Agent - ${agentId.id}")
    

    //TODO: add additional parameters for default consumers
    /**
     * Init default producer
     **/
    protected def initDefaultProducersProperties: Map[String, Properties] = {
        //logger.debug("Init default producers properties")

        val  propsAgentMessageProducer = new Properties()
        propsAgentMessageProducer.put("bootstrap.servers", brokers.mkString(","))
  
        propsAgentMessageProducer.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        defaultSerializer match {
            case Some(serializerClass) => propsAgentMessageProducer.put("value.serializer", serializerClass)
            case None => propsAgentMessageProducer.put("value.serializer", "com.matete.mas.serialization.AgentMessageStringSerializer") //use avro
        }
        propsAgentMessageProducer.put("compression.type", "snappy")

        Map("defaultAgentMessageProducer" -> propsAgentMessageProducer)
    }

    //TODO: add additional parameters for default consumers
 
    /**
     * Init default consumer
     **/
    protected def initDefaultConsumersProperties: Map[String, Properties] = {
        //logger.debug("Init default consumers properties")

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

    /**
     * Init secondary producers
     **/
    protected def initProducersProperties: Map[String, Properties] = {
        //logger.debug("Init producers properties")

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
            case None => //logger.debug("no additional producers")
                Map.empty[String, Properties]

        })
    }

    /**
     * Init secondary consumers
     **/
    protected def initConsumerProperties: Map[String, Properties] = {
        //logger.debug("Init consumer properties")

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
            case None => //logger.debug("no additional consumers")
                Map.empty[String, Properties]
        })
    }


    def propsForProducers: Map[String, Properties] =  initProducersProperties
    def propsForConsumers: Map[String, Properties] = initConsumerProperties

    // take only the agent message producers
    protected def producers: Map[String, KafkaProducer[String, AgentMessage[T]]] = propsForProducers.map{
        case (name,props) => (name, new KafkaProducer[String, AgentMessage[T]](props))
    }

    protected def agentMessageProducer = producers("defaultAgentMessageProducer")  


    // take only the agent message consumers
    protected val consumers: Map[String, KafkaConsumer[String, AgentMessage[T]]] = propsForConsumers.map{
        case (name,props) => (name, new KafkaConsumer[String, AgentMessage[T]](props) )
    }
    protected def agentMessageConsumer = consumers("defaultAgentMessageConsumer")
    
    agentMessageConsumer.subscribe(util.Collections.singletonList(TOPIC))

    // subscribe to the agent topic
    consumers.foreach{
        case(_, kafkaConsumer) => kafkaConsumer.subscribe(util.Collections.singletonList(TOPIC))
    }


    // TODO: keep?
    override def join(agentId: AgentId) = {
        this.agentMessageConsumer.subscribe(util.Collections.singletonList(getTopic(agentId)))
    
    } 

    // TODO: keep?
    override def join(agentIds: List[AgentId]) = {
        this.agentMessageConsumer.subscribe(agentIds.map(getTopic).asJava)
    }

    /**
     * send message to agentIdReceiver
     *  @param agentIdReceiver  agent that the message is sent to
     *  @param message message sent to agentIdReceiver
     * 
     **/
    override  def send(agentIdReceiver: AgentId, message: T, producer: String = "defaultAgentMessageProducer") = {

        logger.debug(s"Sending message to ${agentIdReceiver.id}")
        val record = new ProducerRecord(getTopic(agentIdReceiver), s"${agentIdReceiver.id}", AgentMessage(this.agentId, message))
        this.producers(producer).send(record)
    }


    /**
     * close all the consumers and producers
     **/
    override def die: Unit = {
      logger.info("Agent dying now!")  

      this.consumers.foreach{ case(_, kafkaConsumer) => kafkaConsumer.close }
      this.producers.foreach{ case(_, kafkaProducer) => kafkaProducer.close }

    }


    def forcedie: Unit = this.wantToDie = true

}