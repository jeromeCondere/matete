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

case class AgentId(id: String)

// change to H <: AgentConfig

abstract class AbstractAgent[T](configuration: AgentConfig)( defaultSerializer: Option[String] = None, defaultDeserializer: Option[String] = None) extends AgentLike[T] {

    val agentId: AgentId = AgentId(configuration.id)
    val brokers = configuration.brokers
    val GENERAL_POOL: String = "GENERAL_AGENT_POOL"
    val TOPIC: String = getTopic(AgentId(configuration.id))
    protected var wantToDie: Boolean = false
    protected val stringKeySuffix = "-str"
    final val logger = LogManager.getLogger(s"Agent - ${agentId.id}")

    def initDefaultProducersProperties: Map[String, Properties] = {
         val  propsString = new Properties()
        propsString.put("bootstrap.servers", brokers.mkString(","))
  
        propsString.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        propsString.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        propsString.put("compression.type", "snappy")

        val  propsAgentMessage = new Properties()
        propsAgentMessage.put("bootstrap.servers", brokers.mkString(","))
  
        propsAgentMessage.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        defaultSerializer match {
            case Some(serializerClass) => propsAgentMessage.put("value.serializer", serializerClass)
            case None => propsAgentMessage.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer") //use avro
        }
        propsAgentMessage.put("compression.type", "snappy")

        Map("defaultStringProducer" -> propsString, "defaultAgentMessageProducer" -> propsAgentMessage)

    }

 

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
        defaultDeserializer match {
            case Some(deserializerClass) => propsConsumerAgentMessage.put("value.deserializer", deserializerClass)
            case None => propsConsumerAgentMessage.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer") //use avro
        }
        propsConsumerAgentMessage.put("group.id", getTopicGroupBase(agentId)+"-agent")
        propsConsumerAgentMessage.put("enable.auto.commit", "false")


        Map("defaultStringConsumer" -> propsConsumerString, "defaultAgentMessageConsumer" -> propsConsumerAgentMessage)
    } 


    protected def initProducersProperties: Map[String, Properties] = {
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

    protected def initConsumerProperties: Map[String, Properties] = {
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
    }.filter(_._1 != "defaultStringProducer")
    protected val agentMessageProducer = producers("defaultAgentMessageProducer")  
    protected val stringProducer = new KafkaProducer[String, String](propsForProducers("defaultStringProducer"))


    // take only the agent message consumers
    protected val consumers: Map[String, KafkaConsumer[String, AgentMessage[T]]] = propsForConsumers.map{
        case (name,props) => (name, new KafkaConsumer[String, AgentMessage[T]](props) )
    }.filter(_._1 != "defaultStringConsumer")
    protected val agentMessageConsumer = consumers("defaultAgentMessageConsumer")
    protected val stringConsumer = new KafkaConsumer[String, String](propsForConsumers("defaultStringConsumer"))


    consumers.foreach{
        case(_, kafkaConsumer) => kafkaConsumer.subscribe(util.Collections.singletonList(TOPIC))
    }


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
        
      this.consumers.foreach{ case(_, kafkaProducer) => kafkaProducer.close }
      this.stringConsumer.close
      this.producers.foreach{ case(_, kafkaProducer) => kafkaProducer.close }
      this.stringProducer.close

    }

    def forcedie: Unit = this.wantToDie = true

}