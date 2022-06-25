package com.matete.mas.configuration

case class AgentConfig(
    id: String,
    producers: List[ProducerConfig]
)
  
case class ProducerConfig(
    name: String
)
