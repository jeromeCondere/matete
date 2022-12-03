package com.matete.mas.configuration

case class ExperimentConfig(
    description: Option[String] = None,
    id: String,
    brokers: List[String]
)
