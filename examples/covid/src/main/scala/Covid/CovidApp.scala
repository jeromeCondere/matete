package com.matete.examples.covid

import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import java.util.Properties
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import java.util
import org.apache.logging.log4j.LogManager
import scala.collection.JavaConverters._
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig}
import com.matete.mas.configuration.DefaultConfig.defaultConfig
import com.matete.mas.agent.AgentMessage
import com.matete.mas.agent.simulation.netlogo.NetlogoModel
import com.matete.mas.agent.simulation.netlogo.NetlogoAgent
import scala.io.Source
import org.nlogo.lite.InterfaceComponent
import org.nlogo.lite.InterfaceComponent._
import io.circe.yaml.parser
import io.circe._
import java.io.InputStreamReader
import CovidConfigImplicits._
import org.nlogo.agent._
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.util.Random


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.matete.mas.experiment.ExperimentServerApi
import com.matete.mas.configuration.ExperimentConfig
import com.matete.mas.agent.AgentImplicits._
import scala.collection.mutable.Set

object CovidApp  extends App {


    implicit val system = ActorSystem()

    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

   


    //StdIn.readLine() // let it run until user presses return


    val logger = LogManager.getLogger("CovidApp")
    val broker =  args(0)
    val postgresHost = args(1)
    val serverApiHost = args(2)
    val serverApiPort = args(3).toInt

    val postgresUser = if(args.size == 5) "postgres" else args(4)
    val postgresPassword = if(args.size == 5) "postgres" else args(5)
    val postgresDb = if(args.size == 5) "db" else args(6)

    val modelPath = if(args.size == 5) args(4) else args(7)

    
    def setTopic(name: String) = {
        val topicName = name.toTopic
        val newTopics = List(
            new NewTopic(topicName, 1, 1.toShort) 
        )

        
        val props = new Properties()
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
        val client = AdminClient.create(props)
        val topicsList = client.listTopics().names().get().asScala.filter(_ == topicName )

        if(topicsList.isEmpty) { 
            client.createTopics(newTopics.asJava).values().asScala.map{
                case (topicName, kafkaFuture) =>   kafkaFuture.whenComplete {
                    case (_, throwable: Throwable) if Option(throwable).isDefined => logger.error(s"topic $topicName could'nt be created")
                    case _ => logger.info(s"topic $topicName created")
                } 
            }
        } else {
            logger.info(s"topic $topicName already exists, won't be created")
        }
    }


    val configs = getClass.getClassLoader.getResourceAsStream("covidConfig.yaml")
    val jsons = parser.parseDocuments(new InputStreamReader(configs))


    jsons.head match {
        case Left(x) => logger.error("error parsing")
        case Right(y) => logger.info("the file provided is correct")
    }

    val ckt = CovidExperimentServerApi(broker,serverApiHost, serverApiPort)(jsons, modelPath)

    setTopic("ServerManager")

    val forDB =  new ServerManager(List(broker))(host = postgresHost, user = postgresUser, password =  postgresPassword, db =  postgresDb)
    val forDBThread = new Thread {
        override def run {
           forDB.run
        }
    }
    forDBThread.start

}



object CovidModel {
    def model(title: String, modelPath: String) = {
        NetlogoModel(
            src = modelPath,
            maxTicks = 290,
            width = 1000,
            height = 900,
            title = Some(title)
        )
    }
}



class Covid(brokers: List[String], agentId: AgentId, modelConfig: CovidModelConfig, netlogoModel: NetlogoModel, experimentId: String) 
    extends NetlogoAgent[CovidMessage](defaultConfig(brokers = brokers, agentId = agentId))(
        Some("com.matete.examples.covid.AgentMessageCovidMessageSerializer"),
        Some("com.matete.examples.covid.AgentMessageCovidMessageDeserializer")
     )(netlogoModel) {


    var only = 1

    override def receive(agentMessages: List[AgentMessage[CovidMessage]], consumerName: String) = {
        //reportAndCallback("initial-people", x => logger.info(s"initial-people $x"))

        agentMessages.foreach( agentMessage => {
                cmdLater(s"set initial-people initial-people + ${agentMessage.message.turtles.get.size}")

                agentMessage.message.turtles.get.foreach( turtle =>
                createTurtle(turtle)
            )
        })
    }
    
    override def check = { 
        ticks = ticks + 1
        if(only == 1) {
            reportAndCallback("initial-people", x => logger.info(s"initial-people $x"))
            reportAndCallback("infection-chance", x => logger.info(s"infection-chance $x"))
            reportAndCallback("recovery-chance", x => logger.info(s"recovery-chance $x"))
            reportAndCallback("average-recovery-time", x => logger.info(s"average-recovery-time $x"))
            reportAndCallback("p-travel", x => logger.info(s"p-travel $x"))
            only = only + 1
        }
        //TODO: change 25 to config param
        if(ticks % 25 == 0){
            val  frontiers  =   modelConfig.frontiers          

             reportAndCallback("turtles with [ ready-to-travel ]",
             x => {
                val turtles = x.asInstanceOf[ArrayAgentSet].agents.iterator.toList.map(_.asInstanceOf[org.nlogo.agent.Agent])
                logger.info("count travelers " + turtles.size)

                val turtlesRandomized =  Random.shuffle(turtles)



                val zeroToTurtleSize = (0 to (turtlesRandomized.size -1)).to[ListBuffer]
                val elementsToTake = frontiers.map(frontier => (frontier.pTransmission * zeroToTurtleSize.size).round)

                val indexes = (frontiers zip elementsToTake).foldLeft(List[(String,List[Int])]()){
                    case(acc, (frontier, sizeToTake)) => 
                        //logger.info(s"Partitionning frontier ${frontier.countryId} (${frontier.pTransmission}) taking ${sizeToTake} elements out of ${zeroToTurtleSize.size}")

                        val toRemove = zeroToTurtleSize.take(sizeToTake)
                        val res = acc :+ (frontier.countryId, toRemove.toList)
                        zeroToTurtleSize --= toRemove
                        res
                }

                //logger.info(indexes.map(_._2.size))
                val turtlesToSend = indexes.map(x => (x._1, x._2.map(i => agentTurtleToCovidTurtle(turtlesRandomized(i))) ))
                turtlesToSend.forEach{
                    case (countryId, listTurtle) => send(
                        AgentId(countryId), 
                        CovidMessage(experimentId, turtles = Some(listTurtle),  None ) 
                    )
                }

                // killing those turtles since they are travelling and removing them from initial people
                cmdLater("ask turtles with [ ready-to-travel ] [ die ]")
                cmdLater(s"set initial-people initial-people - ${turtles.size}")

              }
            )    
        }

        //TODO create report monad
        reportAndCallback("count turtles with [ infected? ]", 
            infectedCount => reportAndCallback("count turtles with [ not infected? ]", notInfectedCount => 
                reportAndCallback("count turtles with [ travel? ]", travellers => 
                    reportAndCallback("ticks", ticks =>  send(
                            AgentId("ServerManager"), 
                            CovidMessage(experimentId, None,  Some(CovidModelBehaviour(
                                    infectedCount = infectedCount.asInstanceOf[Double].toInt,
                                    notInfectedCount = notInfectedCount.asInstanceOf[Double].toInt,
                                    country = modelConfig.name,
                                    travellers = travellers.asInstanceOf[Double].toInt,
                                    ticks = ticks.asInstanceOf[Double]
                                )
                            ))
                        )
                    )
                )
            )
        )


    }

    def createTurtle(agentTurtle: CovidTurtle) = {
     val cmdToExecute = s"""  create-turtles 1
          [
            setxy random-xcor random-ycor
            set cured? ${agentTurtle.cured}
            set infected? ${agentTurtle.infected}
            set susceptible? ${agentTurtle.susceptible}


            set travel? true
            set ready-to-travel false
            set shape "butterfly"
            set size 2
            set nb-infected ${agentTurtle.nbInfected}
            set nb-recovered ${agentTurtle.nbRecovered}


            set country "${agentTurtle.country}"
            set recovery-time ${agentTurtle.recoveryTime}
            set infection-length ${agentTurtle.infectionLength}


            assign-color
          ]"""
          cmdLater(cmdToExecute)
    }

    def agentTurtleToCovidTurtle(agentTurtle:org.nlogo.agent.Agent) = {
        CovidTurtle(
            cured = agentTurtle.getTurtleOrLinkVariable("CURED?").asInstanceOf[Boolean],
            infected = agentTurtle.getTurtleOrLinkVariable("INFECTED?").asInstanceOf[Boolean],
            susceptible = agentTurtle.getTurtleOrLinkVariable("SUSCEPTIBLE?").asInstanceOf[Boolean],
            country = agentTurtle.getTurtleOrLinkVariable("COUNTRY").toString,
            recoveryTime =  agentTurtle.getTurtleOrLinkVariable("RECOVERY-TIME").asInstanceOf[Double],
            infectionLength = agentTurtle.getTurtleOrLinkVariable("INFECTION-LENGTH").asInstanceOf[Double],
            nbInfected = agentTurtle.getTurtleOrLinkVariable("NB-INFECTED").asInstanceOf[Double],
            nbRecovered = agentTurtle.getTurtleOrLinkVariable("NB-RECOVERED").asInstanceOf[Double]
        )
    }

    override def setup = {
        cmd(s"set initial-people ${modelConfig.initialPeople}")
        cmd(s"set infection-chance ${modelConfig.infectionChance}")
        cmd(s"set recovery-chance ${modelConfig.recoveryChance}")
        cmd(s"set average-recovery-time ${modelConfig.averageRecoveryTime}")
        cmd(s"set p-travel ${modelConfig.pTravel}")
        cmd(s"""set g-country \"${modelConfig.name}\"""")
    }

}



class CovidExperimentServerApi(broker: String, host: String, port: Int = 1010)(jsons: Stream[Either[ParsingFailure, Json]], modelPath: String)(implicit system: ActorSystem, covidParamFormat: JsonFormat[CovidExperimentParameter]) extends ExperimentServerApi[CovidExperimentParameter](host, port)  {
    override def runExperiment(experimentConfig: ExperimentConfig[CovidExperimentParameter]) = {

        //init covid models from global config            
        val model = jsons.head.flatMap(_.as[GlobalModelConfig]).toOption.foreach(
            globalConfig => globalConfig.models.foreach(
                model =>   {
                    val repeat = if(experimentConfig.parameters.repeat <= 0 ) 1 else experimentConfig.parameters.repeat
                    (1 to repeat).toList.forEach{
                        r => {
                            val repeatSuffix = s"-repeat-$r"
                            val agentId =(model.name+"-"+experimentConfig.id).toLowerCase.replaceAll(" ", "")+repeatSuffix
                            val experimentId = experimentConfig.id+repeatSuffix
                            logger.info(s"setting up model ${model.name} id (${agentId})")

                            setTopic(agentId)

                            val covid = new Covid(List(broker), AgentId(agentId), model, CovidModel.model(s"Covid ${model.name} ${experimentConfig.id}", modelPath), experimentId)
                            logger.info(s"running model ${model.name} id (${agentId})")

                            val covidThread = new Thread {
                                override def run {
                                   covid.run
                                }
                            }
                            covidThread.start

                        }
                    }

                }
            )
        )
    }

    def setTopic(name: String) = {
        val topicName = name.toTopic
        val newTopics = List(
            new NewTopic(topicName, 1, 1.toShort) 
        )

        
        val props = new Properties()
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
        val client = AdminClient.create(props)
        val topicsList = client.listTopics().names().get().asScala.filter(_ == topicName )

        if(topicsList.isEmpty) { 
            client.createTopics(newTopics.asJava).values().asScala.map{
                case (topicName, kafkaFuture) =>   kafkaFuture.whenComplete {
                    case (_, throwable: Throwable) if Option(throwable).isDefined => logger.error(s"topic $topicName could'nt be created")
                    case _ => logger.info(s"topic $topicName created")
                }
            }
        } else {
            logger.info(s"topic $topicName already exists, won't be created")
        }
    }
} 


object CovidExperimentServerApi {

    def apply(broker: String, host: String = "localhost", port: Int = 7070)(jsons: Stream[Either[ParsingFailure, Json]], modelPath: String)(implicit system: ActorSystem): CovidExperimentServerApi = {
        import CovidExperimentParameterJsonProtocol._
        new CovidExperimentServerApi(broker, host, port)(jsons, modelPath)
    }
}