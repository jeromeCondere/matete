package com.matete.mas.agent

object AgentImplicits {
  implicit class Conversions(x: String) {
    def toTopic: String = {
      x + "-topic"
    }
  }
}