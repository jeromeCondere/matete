package com.matete.mas.configuration

case class Parameter(key: String, value: String)

case class AgentConfig(
    description: Option[String] = None,
    id: String,
    producers: List[ProducerConfig],
    consumers: List[ConsumerConfig]

)
  
case class ProducerConfig(
    name: String,
    description: Option[String] = None,
    keySerializer: String =  "org.apache.kafka.common.serialization.StringSerializer",
    valueSerializer: String = "org.apache.kafka.common.serialization.StringSerializer",
    compressionType: String = "snappy",
    additionalParameters: Option[List[Parameter]]
)

case class ConsumerConfig(
    name: String,
    description: Option[String] = None,
    keyDeserializer: String =  "org.apache.kafka.common.serialization.StringDeserializer",
    valueDeserializer: String = "org.apache.kafka.common.serialization.StringDeserializer",
    autoCommit: String = "false",
    groupId: String,
    additionalParameters: Option[List[Parameter]]
)
