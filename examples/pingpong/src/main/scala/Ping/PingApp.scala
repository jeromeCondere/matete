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
    logger.info("sending first ping")
    val broker = if(args.size > 1) args(1) else args(0)
    logger.info(s"broker - $broker")

    val ping = new Ping(List(broker))
    ping.send(AgentId("Pong"), "Ping")
 
    ping.run

  logger.info("end of ping")
}


class Ping(brokers: List[String]) extends Agent(AgentId("Ping"), brokers)()() with  Runnable {
    
    override def receiveSimpleMessages(agentMessages: List[String]) = {
        val e = agentMessages.filter(_ == "Pong") 
        if(e.size>0){
            send(AgentId("Pong"), "Ping")
            logger.info(s"Pong received size ${e.size}, sending Ping")
        }
            
        Thread.sleep(2000)
    }
}