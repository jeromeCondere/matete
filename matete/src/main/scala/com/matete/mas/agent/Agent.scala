package com.matete.mas.agent

import java.util.Properties
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util
import scala.collection.JavaConverters._
import java.time.Duration
import org.apache.kafka.common.errors.WakeupException
import com.matete.mas.configuration.AgentConfig
import com.matete.mas.configuration.DefaultConfig.defaultConfig

class Agent[T](configuration: AgentConfig)
( protected val defaultSerializer: Option[String] = None, protected val defaultDeserializer: Option[String] = None) 
extends AbstractAgent[T](configuration)(defaultSerializer, defaultDeserializer){
    //polling rate
    def pollRate: Duration = Duration.ofMillis(1000)
      /***
       * I 
       ***/
    logger.info("agent started")
    logger.debug(s"Brokers - ${brokers.mkString(", ")}")
    
    class PollingLoopThread extends Runnable {
        override def run(): Unit = pollingLoop
    } 
    
    override def disconnect(agentId: AgentId): Unit = {}

    /***
     * receive method to process incoming agent messages
     * @param agentMessages ordered list of agent messages (from most recent to less recent)
     ***/
    override def receive(agentMessages: List[AgentMessage[T]], consumerName: String = "defaultAgentMessageConsumer" ) = {}

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


                    val recordsAgentMessageList = agentMessageConsumer.poll(this.pollRate).iterator.asScala.toList
                    .filter(_.key!=null)
                    .filterNot(_.key.endsWith(this.stringKeySuffix)).map{
                        record =>  record.value
                    }
                    if(!recordsAgentMessageList.isEmpty)
                        receive(recordsAgentMessageList, "defaultAgentMessageConsumer")

                    consumers.filterNot(tuple => List("defaultStringConsumer", "defaultAgentMessageConsumer").contains(tuple._1)).foreach {
                        case (consumerName, consumer) => val recordsAgentMessageListNotDefault = consumer.poll(this.pollRate).iterator.asScala.toList
                            .filter(_.key!=null)
                            .filterNot(_.key.endsWith(this.stringKeySuffix)).map{
                                record => record.value
                            }
                            if(!recordsAgentMessageListNotDefault.isEmpty)
                                receive(recordsAgentMessageListNotDefault, consumerName)
                            consumer.commitAsync

                    }
                    agentMessageConsumer.commitAsync
                    
                } 
            } catch {
                case  e: WakeupException => logger.info("Wakeup exception")
                
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

    def this(agentId: AgentId, brokers: List[String])
        ( serializer: Option[String], deserializer: Option[String]) = {

            this( 
                defaultConfig(brokers = brokers, agentId = agentId)
            )(serializer, deserializer)
 }
}

object Agent {
    def apply[T](agentId: AgentId, brokers: List[String])
        ( serializer: Option[String] = None, deserializer: Option[String] = None): Agent[T] = {
        new  Agent[T](agentId, brokers)(serializer, deserializer)
    }    
    def apply[T](config :AgentConfig)
        ( serializer: Option[String] , deserializer: Option[String] ): Agent[T] = {
        new  Agent[T](config)(serializer, deserializer)
    }
    def apply[T](agentId: AgentId, brokers: List[String]): Agent[T] = {
        new  Agent[T](agentId, brokers)(None, None)
    }
}
