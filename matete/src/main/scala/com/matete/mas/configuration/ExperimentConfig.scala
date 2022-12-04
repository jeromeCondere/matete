package com.matete.mas.configuration


case class ExperimentConfig[T](
    id: String,
    name: String,
    description: Option[String] = None,
    parameters: T
)
