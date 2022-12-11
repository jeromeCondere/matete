package com.matete.mas.agent

//TODO: add test for agent id
 class AgentId(val id: String) {
 }

case class AgentIdException(message: String) extends Exception(message) 

	
 @throws(classOf[AgentIdException])
 object AgentId{

 	def apply(id: String): AgentId = {

 		id match {
 			case null => throw AgentIdException("Id can't be null")
 			case c if c.exists(_.isUpper) => throw AgentIdException("Id must be lowercase")
 			case c if (c contains " ") => throw AgentIdException("Id must not contains spaces")
 			case _ => new AgentId(id)

 		}
 	}
 }