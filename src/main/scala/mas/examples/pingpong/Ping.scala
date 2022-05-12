package mas.examples.pingpong
package mas.examples

import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util

object PingApp extends App {

    val ping = new Ping
    ping.send(AgentId("Pong"), "Ping")


    ping.run({})
  
}


class Ping extends Agent(AgentId("Ping"), List("localhost:9092"))()() {
    pollRate = 1000
    
    override def receiveSimpleMessages(agentMessages: List[String]) = {
        agentMessages.foreach(t => send(AgentId("Pong"), "Ping"))
    }
}