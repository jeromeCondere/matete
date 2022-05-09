package com.matete.mas.agent

case class AgentMessage[T](agentId: String, message: T)