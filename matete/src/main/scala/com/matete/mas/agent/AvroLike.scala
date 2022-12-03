package com.matete.mas.agent

import org.apache.avro.generic.GenericRecord

trait AvroLike[T] {
  def avroToMessage(records: GenericRecord): AgentMessage[T]
  def messageToAvro(message: T): GenericRecord
}
