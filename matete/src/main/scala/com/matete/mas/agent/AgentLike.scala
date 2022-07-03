package com.matete.mas.agent

trait AgentLike[T] {
    /***
     * send message to another agent
     * 
     ***/
    def init: Unit = {}
    def send(agentIdReceiver: AgentId, message: T)
    def send(agentId: AgentId, agentMessage: String)
    def receive(agentMessages: List[AgentMessage[T]], consumerName: String)
    def receiveSimpleMessages(agentMessages: List[String])

    def suicide: Unit
    def broadcast(message: String)
    def forcedie: Unit
    def die: Unit
    def join(agentId: AgentId)
    def join(agentIds: List[AgentId])
    def disconnect(agentId: AgentId)
    def run
    def pollingLoop
    def getTopic(agentId: AgentId): String = agentId.id+"-topic"
    def getStringTopic(agentId: AgentId): String = agentId.id+"-topic-str"
    def getTopicGroupBase(agentId: AgentId): String = getTopic(agentId)+"-group"
    
}