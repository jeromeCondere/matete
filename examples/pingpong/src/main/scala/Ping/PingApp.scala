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
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig}
import scala.collection.JavaConverters._
import com.matete.mas.configuration.DefaultConfig.defaultConfig
import com.matete.mas.agent.AgentMessage



object PingApp extends App {
    val topicName = "Ping-topic"
    val newTopics = List(
     new NewTopic(topicName, 1, 1.toShort) 
    )


    val logger = LogManager.getLogger("PingApp")
    logger.info("Sending first message ping")

    val broker = if(args.size > 1) args(1) else args(0)
    val props = new Properties()
    
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
    props.put("request.timeout.ms", 70000)
    props.put("default.api.timeout.ms", 90000)

    val client = AdminClient.create(props)
    
    val topicsList = client.listTopics().names().get().asScala.filter(_ == topicName )

    if(topicsList.isEmpty) { // topic doesn't exist
        client.createTopics(newTopics.asJava).values().asScala.map{
            case (topicName, kafkaFuture) =>   kafkaFuture.whenComplete {
                case (_, throwable: Throwable) if Option(throwable).isDefined => logger.error(s"topic $topicName could'nt be created")
                case _ => logger.info(s"topic $topicName created")
            } 
        }
    } else {
        logger.info(s"topic $topicName already exists, won't be created")
    }


    logger.info(s"broker - $broker")

    val ping = new Ping(List(broker))
    ping.send(AgentId("Pong"), "Ping")
    logger.info("start running ping agent")

    ping.run

  logger.info("end of ping")
}


class Ping(brokers: List[String]) extends Agent[String](defaultConfig(brokers = brokers, agentId = AgentId("Ping")))() with  Runnable {
    
    override def receive(agentMessages: List[AgentMessage[String]], consumerName: String) = {
        val e = agentMessages.filter(_.message == "Pong") 
        if(e.size>0){
            send(AgentId("Pong"), "Ping")
            logger.info(s"Pong received size ${e.size}, sending Ping")
        }
            
        Thread.sleep(2000)
    }
}