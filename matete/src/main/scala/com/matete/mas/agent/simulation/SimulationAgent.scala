package com.matete.mas.agent.simulation
import com.matete.mas.agent.Agent
import com.matete.mas.configuration.AgentConfig

/**
 * An agent that runs a simulation
 */
class SimulationAgent[T](configuration: AgentConfig)( defaultSerializer: Option[String] = None, defaultDeserializer: Option[String] = None)extends Agent[T](configuration)(defaultSerializer, defaultDeserializer)  {
 
}
  