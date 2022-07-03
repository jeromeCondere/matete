package com.matete.mas.configuration
import  com.matete.mas.agent.AgentId

case class Parameter(key: String, value: String)

case class AgentConfig(
    description: Option[String] = None,
    id: String,
    brokers: List[String],
    producers: Option[List[ProducerConfig]],
    consumers: Option[List[ConsumerConfig]]

)
  
case class ProducerConfig(
    name: String,
    description: Option[String] = None,
    keySerializer: String =  "org.apache.kafka.common.serialization.StringSerializer",
    valueSerializer: String = "com.matete.mas.serialization.AgentMessageStringSerializer",
    compressionType: String = "snappy",
    additionalParameters: Option[List[Parameter]]
)

case class ConsumerConfig(
    name: String,
    description: Option[String] = None,
    keyDeserializer: String =  "org.apache.kafka.common.serialization.StringDeserializer",
    valueDeserializer: String = "com.matete.mas.deserialization.AgentMessageStringDeserializer",
    autoCommit: String = "false",
    groupId: String,
    additionalParameters: Option[List[Parameter]]
)

object DefaultConfig  {
    def defaultConfig (agentId: AgentId, brokers: List[String]) = {
        AgentConfig(
                description = None, id = agentId.id, brokers =  brokers, 
                producers = None, consumers = None
             )
    }
}