package com.matete.examples.pingpong

import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util
import org.apache.logging.log4j.LogManager



object PongApp extends App {

    val logger = LogManager.getLogger("PingApp")
    logger.info("sending first ping")
    logger.info("end of ping")
    val broker = if(args.size > 1) args(1) else args(0)
    println("broker: "+broker)

    val pong = new Pong(List(broker))
    pong.run

}


class Pong(brokers: List[String]) extends Agent(AgentId("Pong"), brokers)()() with Runnable{
    override def receiveSimpleMessages(agentMessages: List[String]) = {
        val e = agentMessages.filter(_ == "Ping")
        if(e.size>0){
            send(AgentId("Ping"), "Pong")
            logger.info(s"Ping received : ${e.size}, sending Pong")
        }
            
    }
}
