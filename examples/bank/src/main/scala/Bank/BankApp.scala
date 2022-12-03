package com.matete.examples.bank

import com.matete.mas.agent.AvroAgent
import com.matete.mas.agent.AgentId
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import java.util
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
import scala.collection.mutable.ListBuffer

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
         ListBuffer(
             Account("Client1_account", "Client1", 256.5, 1000),
             Account("Client2_account", "Client2", 1000.9, 700)
        )
    )

    bank.run

}

class Bank(brokers: List[String])(accounts:  ListBuffer[Account]) extends AvroAgent[Transaction](defaultConfig(brokers = brokers, agentId = AgentId("Bank")))(null, "http://localhost:8081") with Runnable{
    val schema = Transaction.initSchema 

    override def receive(agentMessages: List[AgentMessage[Transaction]], consumerName: String) = {
        agentMessages.filter(_.message.typeTransaction == "withdrawal").map(_.message).foreach(
            transaction => {
                logger.info(s"id to get: ${transaction.to.get}")
                logger.info(accounts)
                val accountToWithdrawFrom = accounts.find(_.id == transaction.to.get).get
                val amountToWithDraw = transaction.amount
                
                if(amountToWithDraw < accountToWithdrawFrom.money + accountToWithdrawFrom.overdraft){


                    val newMoney = accountToWithdrawFrom.money - amountToWithDraw
                    accounts -= accountToWithdrawFrom
                    accounts += Account(
                        id = accountToWithdrawFrom.id, 
                        owner = accountToWithdrawFrom.owner, 
                        money = newMoney ,
                        overdraft = accountToWithdrawFrom.overdraft
                    ) 
                    send(AgentId(accountToWithdrawFrom.owner), Transaction(
                        label = Some(s"withdrawal accepted"),
                        from = Some(accountToWithdrawFrom.id),
                        to = Some(accountToWithdrawFrom.id),
                        amount = amountToWithDraw,
                        typeTransaction = "withdrawal",
                        message = Some(s"the bank (${agentId.id}) has accepted your request, here's your money")
                        )
                    )
                    logger.info(s"${accountToWithdrawFrom.owner} has withdrawn ${amountToWithDraw} from his account ${accountToWithdrawFrom.id}")
                    logger.info(s"here is his money: ${accounts.find(_.id == transaction.to.get).get.money}")
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
                    logger.info(s"${accountToWithdrawFrom.owner} has failed to withdrawn ${amountToWithDraw} from his account ${accountToWithdrawFrom.id}")
                }
            }
        )

        agentMessages.filter(_.message.typeTransaction == "deposit").map(_.message).foreach(
            transaction => {
                val accountToPutDepositOn = accounts.find(_.id == transaction.to.get).get
                val amountToDeposit = transaction.amount
                val newMoney = accountToPutDepositOn.money + amountToDeposit
                    accounts -= accountToPutDepositOn
                    accounts += Account(
                    id = accountToPutDepositOn.id, 
                    owner = accountToPutDepositOn.owner, 
                    money = newMoney,
                    overdraft = accountToPutDepositOn.overdraft
                ) 
                send(AgentId(accountToPutDepositOn.owner), Transaction(
                    label = Some(s"deposit accepted"),
                    from = Some(accountToPutDepositOn.id),
                    to = Some(accountToPutDepositOn.id),
                    amount = amountToDeposit,
                    typeTransaction = "deposit",
                    message = Some(s"the bank (${agentId.id}) has accepted your request, deposit accepted")
                    )
                )
                logger.info(s"${accountToPutDepositOn.owner} has put ${amountToDeposit} on his account ${accountToPutDepositOn.id}")
                logger.info(s"Here is his money: ${accounts.find(_.id == transaction.to.get).get.money}")
            }


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
