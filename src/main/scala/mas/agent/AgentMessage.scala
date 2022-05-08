package mas.agent

case class AgentMessage[T](agentId: String, message: T)