package com.matete.mas.agent

case class AgentMessage[T](agentId: AgentId, message: T)
