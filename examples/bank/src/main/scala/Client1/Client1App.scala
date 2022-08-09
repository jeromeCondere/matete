package com.matete.examples.bank


import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util
import io.circe.yaml.parser
import java.io.InputStreamReader
import com.matete.mas.configuration.AgentConfig
import com.matete.mas.configuration.AgentConfigImplicits._
import cats.syntax.either._
import org.apache.logging.log4j.LogManager
import scala.collection.JavaConverters._
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig}
import com.matete.mas.configuration.DefaultConfig.defaultConfig
import com.matete.mas.agent.AgentMessage
import com.matete.examples.bank.utils.{Transaction, Client}
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.avro.Schema

object Client1App extends App {
    val topicName = "Client1-topic"
    val newTopics = List(
     new NewTopic(topicName, 1, 1.toShort) 
    )
    

    val logger = LogManager.getLogger("Client1App")
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

    logger.info("start running client1 agent")
    val client1 = new Client1(List(broker), AgentId("Bank"))

    client1.run

}

class Client1(brokers: List[String], bankAgentId: AgentId) extends Client(brokers, AgentId("Client1"), bankAgentId)(145, "Client1_account"){
   override def receive(agentMessages: List[AgentMessage[Transaction]], consumerName: String) = {

        agentMessages.filter(_.message.typeTransaction == "deposit").map(_.message).foreach(
            transaction => {
                val amount = transaction.amount
                if(transaction.label == Some("deposit accepted")){
                    this.moneyOnme = this.moneyOnme - amount
                    logger.info(s"Put deposit $amount on my account, let's goo")
                    logger.info("money " + moneyOnme)
                }
            }
        )

        agentMessages.filter(_.message.typeTransaction == "withdrawal").map(_.message).foreach(
            transaction => {
                val amount = transaction.amount
                if(transaction.label == Some("withdrawal accepted")){
                    this.moneyOnme = this.moneyOnme + amount
                    logger.info(s"The withdrawal of $amount has been accepted for my account, hmmm")
                        logger.info("money " + moneyOnme)
                }
            }
        )
   }
       override def run = {
        init
        logger.info(s"Start polling loop")
        logger.info("initial money " + moneyOnme)
        withdrawal(5)
        Thread.sleep(2000)
        withdrawal(34)
        Thread.sleep(1000)
        withdrawal(245)
        deposit(100, "a volo")
        deposit(45.6, "essou")
        pollingLoop
        logger.info(s"the polling loop is running in the thread")

    }
}
