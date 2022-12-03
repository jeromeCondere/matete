package com.matete.mas.agent.simulation
import com.matete.mas.agent.Agent
import com.matete.mas.configuration.AgentConfig

/**
  * An agent that runs a simulation
  *
  * @param configuration agent config - contains agent id, consumers and producers config
  * @param defaultSerializer serializer used to send the message of type T.
  * @param defaultDeserializer serializer used to receive the message of type T.
  */
abstract class SimulationAgent[T](configuration: AgentConfig)(
    defaultSerializer: Option[String] = None,
    defaultDeserializer: Option[String] = None
) extends Agent[T](configuration)(defaultSerializer, defaultDeserializer) {}
