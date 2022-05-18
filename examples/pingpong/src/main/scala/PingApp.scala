package mas.examples.pingpong
package mas.examples

import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util
import java.time.Duration
import com.typesafe.scalalogging.Logger

object PingApp extends App {

    val logger = Logger(PingApp.getClass())
    logger.error("doe")
    val ping = new Ping
    ping.send(AgentId("Pong"), "Ping")
    println("sending ping")


    ping.run

    
  println("end of ping")
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