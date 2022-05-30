package com.matete.examples.pingpong

import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util
import java.time.Duration
import org.apache.logging.log4j.LogManager

object PingApp extends App {

    val logger = LogManager.getLogger("PingApp")
    val ping = new Ping
    ping.send(AgentId("Pong"), "Ping")
    logger.info("sending first ping")


    ping.run

  logger.info("end of ping")
}


class Ping extends Agent(AgentId("Ping"), List("localhost:9092"))()() with  Runnable {
    
    override def receiveSimpleMessages(agentMessages: List[String]) = {
        val e = agentMessages.filter(_ == "Pong") 
        if(e.size>0){
            send(AgentId("Pong"), "Ping")
            logger.info(s"Pong received size ${e.size}, sending Ping")
        }
            
        Thread.sleep(2000)
    }
}