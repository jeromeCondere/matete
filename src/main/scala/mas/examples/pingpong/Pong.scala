package mas.examples.pingpong
package mas.examples

import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util

object PongApp extends App {

    val pong = new Pong


    pong.run({})
  
}


class Pong extends Agent(AgentId("Pong"), List("localhost:9092"))()() {
    override def receiveSimpleMessages(agentMessages: List[String]) = {
        agentMessages.foreach(t => send(AgentId("Ping"), "Pong"))
    }
}
