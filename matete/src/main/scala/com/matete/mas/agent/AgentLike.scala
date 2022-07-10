package com.matete.mas.agent

trait AgentLike[T] {
    
    def init: Unit = {}
    def send(agentIdReceiver: AgentId, message: T, producer: String)
    def receive(agentMessages: List[AgentMessage[T]], consumerName: String)
    def receiveSimpleMessages(agentMessages: List[String])

    def suicide: Unit
    // def broadcast(message: String)
    def forcedie: Unit
    def die: Unit
    def join(agentId: AgentId)
    def join(agentIds: List[AgentId])
    def disconnect(agentId: AgentId)
    def run
    def pollingLoop
    def getTopic(agentId: AgentId): String = agentId.id+"-topic"
    def getTopicGroupBase(agentId: AgentId): String = getTopic(agentId)+"-group"
    
}