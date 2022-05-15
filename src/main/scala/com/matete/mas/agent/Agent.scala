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

    class ReceiveMessagesTask extends Runnable {
        override def run(): Unit = pollingLoop
    }

      /***
       * I 
       ***/
    override def disconnect(agentId: AgentId): Unit = {}

          /***
       * I 
       ***/
    override def receive(agentMessages: List[AgentMessage[T]]) = {}

    override def receiveSimpleMessages(agentMessages: List[String]) = {}

    override def forcedie: Unit = {}
    override def init: Unit = {}
    override def pollingLoop: Unit = {
        try{
                while(wantToDie == false){


                    val recordsStringList = stringConsumer.poll(this.pollRate).iterator.asScala.toList
                    .filter(c => c.key()!= null && c.key().endsWith(this.stringKeySuffix)).map{
                        record => record.value
                    }
                    receiveSimpleMessages(recordsStringList)

                    val recordsAgentMessageList = agentMessageConsumer.poll(this.pollRate).iterator.asScala.toList
                    .filter(_.key()!=null)
                    .filterNot(_.key().endsWith(this.stringKeySuffix)).map{
                        record =>  println(record.value())
                            record.value
                    }
                    receive(recordsAgentMessageList)

                    stringConsumer.commitAsync
                    agentMessageConsumer.commitAsync
                    
                } 
            } catch {
                case    e: WakeupException => println()
                
            } finally {
                die
            }
    }
    override def run = {
        init
        val receiveMessagesTask = new ReceiveMessagesTask()
        receiveMessagesTask.run()
        print("hey")


    }
    def sendPool(message: String): Unit = ???
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
