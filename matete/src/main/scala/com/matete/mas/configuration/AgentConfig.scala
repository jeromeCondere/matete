package com.matete.mas.configuration

import org.apache.kafka.common.KafkaFuture.BiConsumer



case class AgentConfig(
    id: String,
    producers: List[ProducerConfig],
    consumers: List[ConsumerConfig],
    description: Option[String] = None

)
  
case class ProducerConfig(
    keySerializer: String =  "org.apache.kafka.common.serialization.StringSerializer",
    valueSerializer: String = "org.apache.kafka.common.serialization.StringSerializer",
    compressionType: String = "snappy"
)

case class ConsumerConfig(
    keyDeserializer: String =  "org.apache.kafka.common.serialization.StringDeserializer",
    valueDeserializer: String = "org.apache.kafka.common.serialization.StringDeserializer",
    autoCommit: String = "false",
    groupId: String
)
