package com.matete.mas.agent
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util
import scala.collection.JavaConverters._


class Agent[T](agentId: AgentId, brokers: List[String])
(initProducerProperties: Map[String, Properties] => Map[String, Properties] = id => id,  
    initConsumersProperties:  Map[String, Properties] => Map[String, Properties] = id => id)
(implicit serializer: Option[String] = None, deserializer: Option[String] = None) 
extends AbstractAgent[T](agentId, brokers)(initProducerProperties,initConsumersProperties)(serializer, deserializer){
    //polling rate
    var pollRate: Long = 100

      /***
       * I 
       ***/
    override def disconnect(agentId: AgentId): Unit = {}

          /***
       * I 
       ***/
    override def receive(agentMessages: List[AgentMessage[T]]) = {}

    override def receiveSimpleMessages(agentMessages: List[String]) = {}

    override def forcedie(): Unit = {}
    override def init: Unit = {}
    def run(initFunc: Unit): Unit = {
        try{
            init
                while(wantToDie == false){
                    val recordsString = stringConsumer.poll(this.pollRate).asScala
                        .filter(c => c.key().endsWith(this.stringKeySuffix))

                    val recordsStringList = stringConsumer.poll(this.pollRate).iterator.asScala.toList.map{
                        record => record.value
                    }

                    receiveSimpleMessages(recordsStringList)
                    
                    val recordsAgentMessage = agentMessageConsumer.poll(this.pollRate).asScala
                        .filterNot(c => c.key().endsWith(this.stringKeySuffix))
                    
                    recordsAgentMessage.filterNot(c => c.key().endsWith(this.stringKeySuffix))

                    val recordsAgentMessageList = stringConsumer.poll(this.pollRate).iterator.asScala.toList.map{
                        record => record.value
                    }
                    stringConsumer.commitAsync
                    stringConsumer.commitAsync


                    
                }
            } finally {
                die
            }
    }
    def sendPool(message: String): Unit = ???
    def suicide() = { this.wantToDie = true}
}
object Agent {
    def apply[T](agentId: AgentId, brokers: List[String])
    (initProducerProperties: Map[String, Properties] => Map[String, Properties] = id => id,  
    initConsumersProperties:  Map[String, Properties] => Map[String, Properties] = id => id)
        (implicit serializer: Option[String] = None, deserializer: Option[String] = None): Agent[T] = {
        new  Agent[T](agentId, brokers)(initProducerProperties, initConsumersProperties)(serializer, deserializer)
    }
}
