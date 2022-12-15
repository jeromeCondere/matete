package com.matete.mas.agent

//TODO: add test for agent id
class AgentId(val id: String) {
	override def toString() : String = {
		s"AgentId($id)"
	}
}

case class AgentIdException(message: String) extends Exception(message) 

	
@throws(classOf[AgentIdException])
object AgentId{

 	def apply(id: String): AgentId = {

 		id match {
 			case null => throw AgentIdException("Id can't be null")
 			case c if (c contains " ") => throw AgentIdException("Id must not contains spaces")
 			case c if ((c contains "/" )|| (c contains "|")) => throw AgentIdException("Id must not contains / or |")
 			case _ => new AgentId(id)
 		}
 	}
}