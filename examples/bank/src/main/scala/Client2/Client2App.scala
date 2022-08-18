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
import java.time.Duration


object Client2App extends App {
    val topicName = "Client2-topic"
    val newTopics = List(
     new NewTopic(topicName, 1, 1.toShort) 
    )
    

    val logger = LogManager.getLogger("Client2App")
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

    logger.info("start running client2 agent")
    val client2 = new Client2(List(broker), AgentId("Bank"))
    client2.run
}

class Client2(brokers: List[String], bankAgentId: AgentId) extends Client(brokers, AgentId("Client2"), bankAgentId)(234, "Client2_account") with Runnable{
    override def pollRate: Duration = Duration.ofMillis(100)

    override def receive(agentMessages: List[AgentMessage[Transaction]], consumerName: String) = {
        logger.info("AYO "+ agentMessages)

        agentMessages.filter(_.message.typeTransaction == "deposit").map(_.message).foreach(
            transaction => {
                val amount = transaction.amount
                if(transaction.label.contains("deposit accepted")){
                    this.moneyOnme = this.moneyOnme - amount
                    this.logger.info(s"Put deposit $amount on my account, I'm very seious")
                    logger.info("money " + moneyOnme)
                }
            }
        )

        agentMessages.filter(_.message.typeTransaction == "withdrawal").map(_.message).foreach(
            transaction => {
                val amount = transaction.amount
                if(transaction.label.contains ("withdrawal accepted")){
                    this.moneyOnme = this.moneyOnme + amount
                    this.logger.info(s"The withdrawal of $amount has been accepted for my account, I'm serious")
                    logger.info("money " + moneyOnme)
                }
            }
        )
    }

    override def run = {
        init
        logger.info(s"Start polling loop")
        logger.info("initial money " + moneyOnme)
        logger.info("withdrawal of 5")
        withdrawal(5)
        Thread.sleep(2000)
        
        logger.info("withdrawal of 34")
        withdrawal(34)
        
        Thread.sleep(1000)
        logger.info("withdrawal of 245")

        withdrawal(245)
        deposit(100, "blabla")
        deposit(45.6, "rent")
        pollingLoop
        logger.info(s"the polling loop is running in the thread")
    }

}         
