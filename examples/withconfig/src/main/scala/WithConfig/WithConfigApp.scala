package com.matete.examples.withconfig

import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import java.util.Properties
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import java.util
import io.circe.yaml.parser
import java.io.InputStreamReader
import com.matete.mas.configuration.AgentConfig
import com.matete.mas.configuration.AgentConfigImplicits._
import cats.syntax.either._
import org.apache.logging.log4j.LogManager
import scala.collection.JavaConverters._
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig}

object WithConfigApp extends App {
  val topicName = "WithConfig-topic"
  val newTopics = List(
    new NewTopic(topicName, 1, 1.toShort)
  )

  val logger = LogManager.getLogger("WithConfig")
  val broker = if (args.size > 1) args(1) else args(0)

  logger.info("broker: " + broker)

  val props = new Properties();
  props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
  val client = AdminClient.create(props)
  val topicsList =
    client.listTopics().names().get().asScala.filter(_ == topicName)

  if (topicsList.isEmpty) { // topic doesn't exist
    client.createTopics(newTopics.asJava).values().asScala.map {
      case (topicName, kafkaFuture) =>
        kafkaFuture.whenComplete {
          case (_, throwable: Throwable) if Option(throwable).isDefined =>
            logger.error(s"topic $topicName could'nt be created")
          case _ => logger.info(s"topic $topicName created")
        }
    }
  } else {
    logger.info(s"topic $topicName already exists, won't be created")
  }
  val configs = getClass.getClassLoader.getResourceAsStream("config.yaml")
  val jsons = parser.parseDocuments(new InputStreamReader(configs))
  jsons.foreach {
    case Right(x) => println(x.as[AgentConfig])
    case Left(y)  => println()
  }

}
