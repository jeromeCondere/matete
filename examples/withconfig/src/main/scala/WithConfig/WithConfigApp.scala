package com.matete.examples.withconfig

import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util

import scala.collection.JavaConverters._


object WithConfigApp extends App {

    val configs = getClass.getClassLoader.getResourceAsStream("config.yaml")
  
}
