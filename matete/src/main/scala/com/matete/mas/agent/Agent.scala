package com.matete.mas.agent

import java.util.Properties
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import java.util
import scala.collection.JavaConverters._
import java.time.Duration
import org.apache.kafka.common.errors.WakeupException
import com.matete.mas.configuration.AgentConfig
import com.matete.mas.configuration.DefaultConfig.defaultConfig

/** Agent class
  *
  * An agent is a component using kafka stream to communicate with other agent
  * An agent can have several consumers and producers
  *
  * @constructor Create an agent that can only send and receive messages of type T
  * @param configuration agent config - contains agent id, consumers and producers config
  * @param defaultSerializer serializer used to send the message of type T.
  * @param defaultDeserializer serializer used to receive the message of type T.
  */
class Agent[T](configuration: AgentConfig)(
    protected val defaultSerializer: Option[String] = None,
    protected val defaultDeserializer: Option[String] = None
) extends AbstractAgent[T](configuration)(
      defaultSerializer,
      defaultDeserializer
    ) {

  /**polling rate */
  def pollRate: Duration = Duration.ofMillis(100)

  logger.info("Agent started")
  logger.debug(s"Brokers - ${brokers.mkString(", ")}")

  class PollingLoopThread extends Runnable {
    override def run(): Unit = pollingLoop
  }

  /**
    * receive method to process incoming agent messages
    * @param agentMessages ordered list of agent messages (from most recent to less recent)
    * @param consumerName name of the consumer that process the list of message
       **/
  override def receive(
      agentMessages: List[AgentMessage[T]],
      consumerName: String = "defaultAgentMessageConsumer"
  ) = {}

  override def forcedie: Unit = {}
  override def init: Unit = {}

  /**
    * Loop used to process the incoming messages
    *
       **/
  override def pollingLoop: Unit = {
    try {
      while (wantToDie == false) {

        val recordsAgentMessageList = agentMessageConsumer
          .poll(this.pollRate)
          .iterator
          .asScala
          .toList
          .filter(_.key != null)
          .filterNot(_.key.endsWith(this.stringKeySuffix))
          .map { record =>
            record.value
          }
        if (!recordsAgentMessageList.isEmpty)
          receive(recordsAgentMessageList, "defaultAgentMessageConsumer")

        consumers
          .filterNot(
            tuple =>
              List("defaultStringConsumer", "defaultAgentMessageConsumer")
                .contains(tuple._1)
          )
          .foreach {
            case (consumerName, consumer) =>
              val recordsAgentMessageListNotDefault = consumer
                .poll(this.pollRate)
                .iterator
                .asScala
                .toList
                .filter(_.key != null)
                .filterNot(_.key.endsWith(this.stringKeySuffix))
                .map { record =>
                  record.value
                }
              if (!recordsAgentMessageListNotDefault.isEmpty)
                receive(recordsAgentMessageListNotDefault, consumerName)

              consumer.commitAsync

          }
        agentMessageConsumer.commitAsync

      }
    } catch {
      case e: WakeupException => logger.error("Wakeup exception")

    } finally {
      logger.debug("Polling loop finished")
      die
    }
  }

  override def run = {
    init
    logger.info(s"Start polling loop")
    pollingLoop
    logger.info(s"the polling loop is running in the thread")

  }

  /**
    * Terminates polling loop
    **/
  def terminate = { this.wantToDie = true }

  def this(
      agentId: AgentId,
      brokers: List[String]
  )(serializer: Option[String], deserializer: Option[String]) = {
    this(
      defaultConfig(brokers = brokers, agentId = agentId)
    )(serializer, deserializer)
  }

}

object Agent {
  def apply[T](agentId: AgentId, brokers: List[String])(
      serializer: Option[String] = None,
      deserializer: Option[String] = None
  ): Agent[T] = {
    new Agent[T](agentId, brokers)(serializer, deserializer)
  }
  def apply[T](
      config: AgentConfig
  )(serializer: Option[String], deserializer: Option[String]): Agent[T] = {
    new Agent[T](config)(serializer, deserializer)
  }
  def apply[T](agentId: AgentId, brokers: List[String]): Agent[T] = {
    new Agent[T](agentId, brokers)(None, None)
  }
}
