package com.matete.examples.bank.utils

import com.matete.mas.agent.AvroAgent
import com.matete.mas.agent.AgentId
import com.matete.mas.configuration.DefaultConfig.defaultConfig
import com.matete.examples.bank.utils.Transaction
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.Schema
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.Decoder
import com.matete.mas.agent.AgentMessage


trait  ClientLike {

    def deposit(amount: Double, label: String)
    def withdrawal(amount: Double)
    //def sendToOther(agentId: AgentId, amount: Double, label: String)

}

abstract class Client(brokers: List[String], agentId: AgentId, bankAgentId: AgentId)( var moneyOnme: Double, val accountId: String) extends AvroAgent[Transaction](defaultConfig(brokers = brokers, agentId = agentId))(null,"http://localhost:8081") 
    with ClientLike {

    val schema = Transaction.initSchema 
    /**
     *  send to the bank the amount
     * 
     **/
    def deposit(amount: Double, label: String) = {
        if( amount <= moneyOnme)
            this.send(bankAgentId,
                Transaction(
                    label = Some(s"${agentId.id}-deposit"),
                    from = Some(s"${accountId}"),
                    to = Some(s"${accountId}"),
                    amount = amount,
                    typeTransaction = "deposit",
                    message = Some(s"as client ${agentId.id} I make a deposit")
                )
            )
        else
            this.logger.error("amount > money owned")
    }

    /**
     *  remove money of account
     **/
    def  withdrawal(amount: Double) = {
        this.send(bankAgentId,
            Transaction(
                label = Some(s"${agentId.id}-withdrawal"),
                from = Some(s"${accountId}"),
                to = Some(s"${accountId}"),
                amount = amount,
                typeTransaction = "withdrawal",
                message = Some(s"as client ${agentId.id}")
            )
        )
    }

    override def messageToAvro(message: Transaction) = {

        val avroRecord = new GenericData.Record(schema)

        val agentIdSchema = schema.getField("agentId").schema()
        val messageSchema = schema.getField("message").schema()


        val agentIdRecord = new GenericData.Record(schema.getField("agentId").schema())
        val messageRecord = new GenericData.Record(schema.getField("message").schema())

        agentIdRecord.put("id", this.agentId.id)
        messageRecord.put("label",message.label.getOrElse(null))
        messageRecord.put("from",message.from.getOrElse(null))
        messageRecord.put("to",message.to.getOrElse(null))
        messageRecord.put("amount", message.amount)
        messageRecord.put("typeTransaction",message.typeTransaction)
        messageRecord.put("message",message.message.getOrElse(null))


        val parser = new Schema.Parser()
        avroRecord.put("agentId", agentIdRecord)
        avroRecord.put("message", messageRecord)
        avroRecord
   }

   override def avroToMessage(record: GenericRecord) = {
        val agentIdRecord = record.get("agentId").asInstanceOf[GenericRecord]
        val messageRecord = record.get("message").asInstanceOf[GenericRecord]

        AgentMessage(
            AgentId(agentIdRecord.get("id").toString), 
                Transaction(
                    label = Option(messageRecord.get("label").toString),
                    from = Option(messageRecord.get("from").toString),
                    to = Option(messageRecord.get("to").toString),
                    amount = messageRecord.get("amount").toString.toDouble,
                    typeTransaction = messageRecord.get("typeTransaction").toString,
                    message = Option(messageRecord.get("message").toString)
        ))
        
   }
 }