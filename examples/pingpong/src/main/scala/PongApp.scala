package com.matete.examples.pingpong

import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util
import com.typesafe.scalalogging.Logger


object PongApp extends App {

    val pong = new Pong
    pong.run
  
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
