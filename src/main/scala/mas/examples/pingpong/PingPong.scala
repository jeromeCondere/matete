package mas.examples

import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util
object PingPong extends App{

    val ping = new Ping
    val pong = new Pong

    val monitor = new Monitor

    ping.run({})
    pong.run({})
    monitor.run

    println("I'm here")
    ping.send(AgentId("Pong"), "Ping")
  
}


class Ping extends Agent(AgentId("Ping"), List("localhost:9092"))()() 
class Pong extends Agent(AgentId("Pong"), List("localhost:9092"))()() {
    override def receiveSimpleMessages(agentMessages: List[String]) = {
        agentMessages.foreach(t => send(AgentId("Ping"), "Pong"))
    }
}

class Monitor { 


 def run = {

    val  props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    
    props.put("group.id", "myGroup")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer")

    val consumerString = new KafkaConsumer[String, String](props)
    consumerString.subscribe(util.Collections.singletonList("Ping-topic"))
    consumerString.subscribe(util.Collections.singletonList("Pong-topic"))
    try{
        while(true){
            val records = consumerString.poll(100)
            records.forEach{ record =>
                println(s"topic = ${record.topic()}, partition = ${record.partition()}, offset = ${record.offset()}, key = ${record.key()}, value = ${record.value()}\n")
            }      
        }
    } finally {
        consumerString.close()
    }
 }

}