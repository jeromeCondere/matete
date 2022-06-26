package com.matete.examples.withconfig

import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util
import io.circe.yaml.parser
import java.io.InputStreamReader
import com.matete.mas.configuration.AgentConfig
import com.matete.mas.configuration.AgentConfigImplicits._
import cats.syntax.either._
import io.circe._
//import io.circe.generic.auto._

import scala.collection.JavaConverters._


object WithConfigApp extends App {

    val configs = getClass.getClassLoader.getResourceAsStream("config.yaml")
    val jsons = parser.parseDocuments(new InputStreamReader(configs))
    jsons.foreach{
        case Right(x) => println(x.as[AgentConfig])
        case Left(y) => println()
    }

  
}
