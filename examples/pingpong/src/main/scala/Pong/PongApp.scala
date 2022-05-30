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

    val pong = new Pong
    pong.run

    logger.info("sending first ping")
    logger.info("end of ping")

  
}


class Pong extends Agent(AgentId("Pong"), List("localhost:9092"))()() with Runnable{
    override def receiveSimpleMessages(agentMessages: List[String]) = {
        val e = agentMessages.filter(_ == "Ping")
        if(e.size>0){
            send(AgentId("Ping"), "Pong")
            logger.info(s"Ping received : ${e.size}, sending Pong")
        }
            
    }
}
