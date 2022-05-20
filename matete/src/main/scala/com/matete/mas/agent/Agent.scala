package com.matete.mas.agent
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util
import scala.collection.JavaConverters._
import java.time.Duration
import org.apache.kafka.common.errors.WakeupException


class Agent[T](agentId: AgentId, brokers: List[String])
(initProducerProperties: Map[String, Properties] => Map[String, Properties] = id => id,  
    initConsumersProperties:  Map[String, Properties] => Map[String, Properties] = id => id)
(implicit serializer: Option[String] = None, deserializer: Option[String] = None) 
extends AbstractAgent[T](agentId, brokers)(initProducerProperties,initConsumersProperties)(serializer, deserializer){
    //polling rate
    var pollRate: Duration = Duration.ofMillis(1000)
    

      /***
       * I 
       ***/
    logger.info("agent started")
    
    override def disconnect(agentId: AgentId): Unit = {}

    /***
     * receive method to process incoming agent messages
     * @param agentMessages ordered list of agent messages (from most recent to less recent)
     ***/
    override def receive(agentMessages: List[AgentMessage[T]]) = {}

    /***
     * receive method to process incoming string messages
     * @param agentMessages ordered list of string messages (from most recent to less recent)
     ***/
    override def receiveSimpleMessages(agentMessages: List[String]) = {}

    override def forcedie: Unit = {}
    override def init: Unit = {}
    override def pollingLoop: Unit = {
        try{
                while(wantToDie == false){


                    val recordsStringList = stringConsumer.poll(this.pollRate).iterator.asScala.toList
                    .filter(c => c.key!= null && c.key.endsWith(this.stringKeySuffix)).map{
                        record => record.value
                    }
                    receiveSimpleMessages(recordsStringList)

                    val recordsAgentMessageList = agentMessageConsumer.poll(this.pollRate).iterator.asScala.toList
                    .filter(_.key!=null)
                    .filterNot(_.key.endsWith(this.stringKeySuffix)).map{
                        record =>  println(record.value)
                            record.value
                    }
                    receive(recordsAgentMessageList)

                    stringConsumer.commitAsync
                    agentMessageConsumer.commitAsync
                    
                } 
            } catch {
                case    e: WakeupException => logger.info("Wakeup exception")
                
            } finally {
                die
            }
    }
    override def run = {
        init
        logger.info(s"Start polling loop")
        pollingLoop
        logger.info(s"the polling loop is running in the thread")

    }
    def suicide = { this.wantToDie = true}
}

object Agent {
    def apply[T](agentId: AgentId, brokers: List[String])
    (initProducerProperties: Map[String, Properties] => Map[String, Properties] = id => id,  
    initConsumersProperties:  Map[String, Properties] => Map[String, Properties] = id => id)
        (implicit serializer: Option[String] = None, deserializer: Option[String] = None): Agent[T] = {
        new  Agent[T](agentId, brokers)(initProducerProperties, initConsumersProperties)(serializer, deserializer)
    }
}
