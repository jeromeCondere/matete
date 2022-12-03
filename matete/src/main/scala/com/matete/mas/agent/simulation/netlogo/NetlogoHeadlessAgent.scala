package com.matete.mas.agent.simulation.netlogo


import scala.concurrent._

import org.nlogo.core.CompilerException
import org.nlogo.api.LogoException
import com.matete.mas.configuration.AgentConfig
import com.matete.mas.agent.simulation.SimulationAgent
import org.nlogo.headless.HeadlessWorkspace
import org.nlogo.core._



/**
 * A class used to create a classic headless netlogoAgent that run the model .nlogo file
 * 
 * @constructor Create an netlogo agent that can only send and receive messages of type T 
 * @param configuration agent config - contains agent id, consumers and producers config
 * @param defaultSerializer serializer used to send the message of type T.
 * @param defaultDeserializer serializer used to receive the message of type T.
 * @param netlogoModel the model used to run the agent
 */
abstract class NetlogoHeadlessAgent[T](configuration: AgentConfig)( defaultSerializer: Option[String] = None, defaultDeserializer: Option[String] = None)(netlogoModel: NetlogoModel) extends SimulationAgent[T](configuration)( defaultSerializer, defaultDeserializer) {


    final protected val workspace = HeadlessWorkspace.newInstance
    var ticks: Double = 0

    val pollingLoopThread = new Thread {
        override def run {
            pollingLoop
        }
    }

  

    /**setup function before running the netlogo model*/
    def setup
  
    /**function called whenever the netlogo ticks*/
    def check

    def runModel = {
        cmd("setup")
        while(ticks < netlogoModel.maxTicks){
            check
            cmd(s"go")
        }
        workspace.dispose
    }



    /**Call when the closing windows event has been triggered*/
    def onClosingWindows = {}

    final def cmd(command: String): Unit = workspace.command(command)
    final def report(source: String): AnyRef  = workspace.report(source)


    val netlogoThread = new Thread {
        override def run {
            java.awt.EventQueue.invokeAndWait(
                new Runnable {
                    override def run {
                        try {
                             workspace.open(netlogoModel.src)
                        } catch {
                            case e: Exception => logger.error("error when opening model")
                                throw e
                        }
                        
                        logger.info("model opened")
                    }
                }  
            )
            setup
            runModel
        }
    }

  /**Runs the netlogo model*/
    override def run = {
        init

        logger.info(s"Start polling loop thread" )
        pollingLoopThread.start

        logger.info(s"Start netlogo thread")
        netlogoThread.start


        pollingLoopThread.join
        netlogoThread.join
        
        logger.info(s"the polling loop thread has finished")
        logger.info(s"the netlogo thread has finished")

    }
}