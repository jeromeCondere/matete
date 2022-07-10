package com.matete.examples.bank

import com.matete.mas.agent.AvroAgent
import com.matete.mas.agent.AgentId
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util
import io.circe.yaml.parser
import java.io.InputStreamReader
import com.matete.mas.configuration.AgentConfig
import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import com.matete.mas.configuration.AgentConfigImplicits._
import cats.syntax.either._
import org.apache.logging.log4j.LogManager
import scala.collection.JavaConverters._
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig}
import com.matete.mas.configuration.DefaultConfig.defaultConfig
import com.matete.mas.agent.AgentMessage
import com.matete.examples.bank.utils.{Transaction, Account}
import scala.collection.mutable.Map
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.Schema
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.Decoder
import com.matete.mas.agent.AgentMessage

object BankApp extends App {
    val topicName = "Bank-topic"
    val newTopics = List(
     new NewTopic(topicName, 1, 1.toShort) 
    )
    

    val logger = LogManager.getLogger("Bank")
    val broker = if(args.size > 1) args(1) else args(0)


    logger.info("broker: "+broker)
    
    val props = new Properties();
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
    val client = AdminClient.create(props)
    val topicsList = client.listTopics().names().get().asScala.filter(_ == topicName )

    if(topicsList.isEmpty) { // topic doesn't exist
        client.createTopics(newTopics.asJava).values().asScala.map{
            case (topicName, kafkaFuture) =>   kafkaFuture.whenComplete {
                case (_, throwable: Throwable) if Option(throwable).isDefined => logger.error(s"topic $topicName could'nt be created")
                case _ => logger.info(s"topic $topicName created")
            } 
        }
    } else {
        logger.info(s"topic $topicName already exists, won't be created")
    }

    logger.info("start running bank agent")

    val bank = new Bank(List(broker))(
        Map(
            ("Client1", Account("Client1", "ckr", 23.5, 100)),
            ("Client1", Account("Client2", "iou", 23.9, 700))
        )
    )

    bank.run


  
}

class Bank(brokers: List[String])(accounts: Map[String, Account]) extends AvroAgent[Transaction](defaultConfig(brokers = brokers, agentId = AgentId("Bank")))(null, "http://localhost:8081") with Runnable{
    val schema = Transaction.initSchema 

    override def receive(agentMessages: List[AgentMessage[Transaction]], consumerName: String) = {
        agentMessages.filter(_.message.typeTransaction == "withdrawal").map(_.message).foreach(
            transaction => {
                val accountToWithdrawFrom = accounts(transaction.to.get)
                val amountToWithDraw = transaction.amount
                if(amountToWithDraw < accountToWithdrawFrom.money + accountToWithdrawFrom.overdraft){
                    accounts(transaction.to.get) = Account(
                        id = accountToWithdrawFrom.id, 
                        name = accountToWithdrawFrom.id, 
                        money = accountToWithdrawFrom.money - amountToWithDraw ,
                        overdraft = accountToWithdrawFrom.overdraft
                    ) 
                    send(AgentId(transaction.from.get), Transaction(
                        label = Some(s"withdrawal accepted"),
                        from = Some(s"$agentId"),
                        to = transaction.from,
                        amount = amountToWithDraw,
                        typeTransaction = "withdrawal",
                        message = Some(s"the bank (${agentId.id}) has accepted your request, here's your money")
                        )
                    )
                    logger.info(s"${transaction.to.get} has withdrawn ${accountToWithdrawFrom.money} from his account ${accountToWithdrawFrom.id}")
                } else {
                    send(AgentId(transaction.from.get), Transaction(
                        label = Some(s"withdrawal refused"),
                        from = Some(s"$agentId"),
                        to = transaction.from,
                        amount = amountToWithDraw,
                        typeTransaction = "withdrawal",
                        message = Some(s"the bank (${agentId.id}) has refused your request")
                        )
                    )
                    logger.info(s"${transaction.to.get} has failed to withdrawn ${accountToWithdrawFrom.money} from his account ${accountToWithdrawFrom.id}")
                }
            }
        )

        agentMessages.filter(_.message.typeTransaction == "deposit").map(_.message).foreach(
            transaction => {
                val accountToPutDepositOn = accounts(transaction.to.get)
                val amountToDeposit = transaction.amount
                accounts(transaction.to.get) = Account(
                    id = accountToPutDepositOn.id, 
                    name = accountToPutDepositOn.id, 
                    money = accountToPutDepositOn.money + amountToDeposit,
                    overdraft = accountToPutDepositOn.overdraft
                ) 
                send(AgentId(transaction.from.get), Transaction(
                    label = Some(s"deposit accepted"),
                    from = Some(s"$agentId"),
                    to = transaction.from,
                    amount = amountToDeposit,
                    typeTransaction = "deposit",
                    message = Some(s"the bank (${agentId.id}) has accepted your request, deposit accepted")
                    )
                )
                logger.info(s"${transaction.to.get} has put ${accountToPutDepositOn.money} from his account ${accountToPutDepositOn.id}")
            }


        )
        
    }

    override def messageToAvro(message: Transaction) = {

        val avroRecord = new GenericData.Record(schema)

        val agentIdSchema = schema.getField("agentId").schema()
        val messageSchema = schema.getField("message").schema()


        val agentIdRecord = new GenericData.Record(schema.getField("agentId").schema())
        val messageRecord = new GenericData.Record(schema.getField("message").schema())

        agentIdRecord.put("id", this.agentId)
        messageRecord.put("label",message.label.getOrElse(null))
        messageRecord.put("from",message.from.getOrElse(null))
        messageRecord.put("to",message.to.getOrElse(null))
        messageRecord.put("amount", message.amount)
        messageRecord.put("typeTransaction",message.typeTransaction)
        messageRecord.put("message",message.message)


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
