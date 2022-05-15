package mas.examples

import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util

import scala.collection.JavaConverters._


object PingPong extends App{

    val ping = new Ping
    val pong = new Pong

    val monitor = new Monitor

    ping.run
    pong.run
    monitor.run

    println("I'm here")
    ping.send(AgentId("Pong"), "Ping")
  
}

class Ping extends Agent(AgentId("Ping"), List("localhost:9092"))()() with  Runnable {
    //pollRate = Duration.ofMillis(100)
    
    override def receiveSimpleMessages(agentMessages: List[String]) = {
        val e = agentMessages.filter(_ == "Pong") 
        if(e.size>0){
            send(AgentId("Pong"), "Ping")
            println(s"Pong received size ${e.size}")
        }
            
        Thread.sleep(2000)
    }
    override def run = super.run
}

class Pong extends Agent(AgentId("Pong"), List("localhost:9092"))()() with Runnable{
    override def receiveSimpleMessages(agentMessages: List[String]) = {
        val e = agentMessages.filter(_ == "Ping")
        if(e.size>0){
            send(AgentId("Ping"), "Pong")
            println(s"Ping received : ${e.size}")
        }
            
    }
    override def run = super.run
}

class Monitor { 


 def run = {

    val  props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    
    props.put("group.id", "myGroup")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer")
    props.put("enable.auto.commit", "false")


    val consumerString = new KafkaConsumer[String, String](props)
    consumerString.subscribe(List("Ping-topic", "Pong-topic").asJava)
    try{
        while(true){
            val records = consumerString.poll(100)
            records.forEach{ record =>
                println(s"topic = ${record.topic()}, partition = ${record.partition()}, offset = ${record.offset()}, key = ${record.key()}, value = ${record.value()}\n")
            }
            consumerString.commitAsync      
        }
    } finally {
        consumerString.close()
    }
 }
}