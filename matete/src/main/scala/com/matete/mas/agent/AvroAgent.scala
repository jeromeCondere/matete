package com.matete.mas.agent

import java.util.Properties
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import java.util
import scala.collection.JavaConverters._
import org.apache.kafka.common.errors.WakeupException
import com.matete.mas.configuration.AgentConfig
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord

/** AvroAgent class
  *
  * An  avro agent is an agent sending avro message
  *
  * @constructor Create an  avro agent that can only send and receive avro messages of type T
  * @param configuration agent config - contains agent id, consumers and producers config
  * @param schema schema of the avro message (should match AgentMessage[T])
  * @param schemaRegistryUrl schema registry url
  */
abstract class AvroAgent[T](configuration: AgentConfig)(
    schema: Schema,
    schemaRegistryUrl: String
) extends Agent[T](configuration)(
      defaultSerializer = None,
      defaultDeserializer = None
    )
    with AvroLike[T] {
  override val defaultSerializer = Some(
    "io.confluent.kafka.serializers.KafkaAvroSerializer"
  )
  override val defaultDeserializer = Some(
    "io.confluent.kafka.serializers.KafkaAvroDeserializer"
  )

  override def initDefaultProducersProperties: Map[String, Properties] = {

    val propsAgentMessageProducer = new Properties()
    propsAgentMessageProducer.put("bootstrap.servers", brokers.mkString(","))

    propsAgentMessageProducer.put(
      "key.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )

    propsAgentMessageProducer.put(
      "value.serializer",
      "io.confluent.kafka.serializers.KafkaAvroSerializer"
    ) //use avro
    propsAgentMessageProducer.put("schema.registry.url", schemaRegistryUrl) //use avro
    propsAgentMessageProducer.put("compression.type", "snappy")

    //logger.debug("init default producer properties avro")
    Map("defaultAgentMessageProducer" -> propsAgentMessageProducer)
  }

  override def initDefaultConsumersProperties: Map[String, Properties] = {

    val propsConsumerAgentMessage = new Properties()
    propsConsumerAgentMessage.put("bootstrap.servers", brokers.mkString(","))
    propsConsumerAgentMessage.put(
      "group.id",
      getTopicGroupBase(agentId) + "-classic"
    )

    propsConsumerAgentMessage.put(
      "key.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer"
    )
    propsConsumerAgentMessage.put(
      "value.deserializer",
      "io.confluent.kafka.serializers.KafkaAvroDeserializer"
    )
    propsConsumerAgentMessage.put(
      "group.id",
      getTopicGroupBase(agentId) + "-agent"
    )
    propsConsumerAgentMessage.put("enable.auto.commit", "false")
    propsConsumerAgentMessage.put("schema.registry.url", schemaRegistryUrl)
    propsConsumerAgentMessage.put("specific.avro.reader", "true")

    //logger.debug("init consumers properties avro")
    Map("defaultAgentMessageConsumer" -> propsConsumerAgentMessage)
  }

  override protected def producers
      : Map[String, KafkaProducer[String, AgentMessage[T]]] =
    propsForProducers
      .filter(_._1 != "defaultAgentMessageProducer")
      .map {
        case (name, props) =>
          (name, new KafkaProducer[String, AgentMessage[T]](props))
      }

  protected val agentAvroMessageProducer =
    new KafkaProducer[String, GenericRecord](
      initDefaultProducersProperties("defaultAgentMessageProducer")
    )

  // take only the agent message consumers
  override protected val consumers
      : Map[String, KafkaConsumer[String, AgentMessage[T]]] = propsForConsumers
    .filter(_._1 != "defaultAgentMessageConsumer")
    .map {
      case (name, props) =>
        (name, new KafkaConsumer[String, AgentMessage[T]](props))
    }

  protected val agentAvroMessageConsumer =
    new KafkaConsumer[String, GenericRecord](
      initDefaultConsumersProperties("defaultAgentMessageConsumer")
    )
  agentAvroMessageConsumer.subscribe(util.Collections.singletonList(TOPIC))
  logger.info("topic = " + TOPIC)

  override def send(
      agentIdReceiver: AgentId,
      message: T,
      producer: String = "defaultAgentMessageProducer"
  ) = {
    logger.debug(s"Sending message to ${agentIdReceiver.id}")
    val topicReceiver = getTopic(agentIdReceiver)

    if (producer == "defaultAgentMessageProducer") {
      val avroRecord = messageToAvro(message)
      val record = new ProducerRecord(
        getTopic(agentIdReceiver),
        s"${agentIdReceiver.id}",
        avroRecord
      )
      this.agentAvroMessageProducer.send(record)
    } else {
      val record = new ProducerRecord(
        getTopic(agentIdReceiver),
        s"${agentIdReceiver.id}",
        AgentMessage(this.agentId, message)
      )
      this.producers(producer).send(record)
    }
  }

  override def pollingLoop: Unit = {
    try {
      while (wantToDie == false) {

        val recordsAgentMessageListPolled =
          agentAvroMessageConsumer.poll(this.pollRate).iterator.asScala.toList

        if (!recordsAgentMessageListPolled.isEmpty) {
          logger.info("message received: " + recordsAgentMessageListPolled)

        }

        val recordsAgentMessageList = recordsAgentMessageListPolled
          .filter(_.key != null)
          .filterNot(_.key.endsWith(this.stringKeySuffix))
          .map { record =>
            record.value
          }
          .map(avroToMessage)

        agentAvroMessageConsumer.poll(this.pollRate).iterator.asScala.toList

        if (!recordsAgentMessageList.isEmpty) {
          logger.info("message received: " + recordsAgentMessageList)
          receive(recordsAgentMessageList, "defaultAgentMessageConsumer")
        }

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
        agentAvroMessageConsumer.commitAsync

      }
    } catch {
      case e: WakeupException => logger.info("Wakeup exception")

    } finally {
      die
    }
  }

}
