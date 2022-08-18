package com.matete.mas.agent.simulation.netlogo


import scala.concurrent._
import java.awt.Point

import org.nlogo.lite.InterfaceComponent
import org.nlogo.lite.InterfaceComponent._
import org.nlogo.core.CompilerException
import org.nlogo.api.LogoException
import com.matete.mas.configuration.AgentConfig
import com.matete.mas.agent.simulation.SimulationAgent
import org.nlogo.api.NetLogoListener 
import org.nlogo.core._

//TODO: REMOVE MAX TICKS AND FPS
//TODO: ADD TITLE
/**
 * A class used to create a classic netlogoAgent that run the model .nlogo file
 * 
 * @constructor 
 * @param netlogoModel the model used to run the agent
 * @param fps frames per second 
 */
abstract class NetlogoAgent[T](configuration: AgentConfig)( defaultSerializer: Option[String] = None, defaultDeserializer: Option[String] = None)(netlogoModel: NetlogoModel) extends SimulationAgent[T](configuration)( defaultSerializer, defaultDeserializer) {

    final protected  val frame = new javax.swing.JFrame 

    
    final protected  val comp = new InterfaceComponent(frame)
    var ticks: Double = 0

    val pollingLoopThread = new Thread {
        override def run {
            pollingLoop
        }
    }  


    frame.addWindowListener(new java.awt.event.WindowAdapter {
    override def windowClosing(e: java.awt.event.WindowEvent) = {
        onClosingWindows
    }
    })

  

    /**setup function before running the netlogo model*/
    def setup
  
    /**function called whenever the netlogo ticks*/
    def check

    def runModel = {
        cmd("setup")
        cmd(s"repeat ${netlogoModel.maxTicks} [ go ]")
    }



    /**Call when the closing windows event has been triggered*/
    def onClosingWindows = {}

    final def cmd(command: String): Unit = comp.command(command)
    final def cmdLater(command: String): Unit = comp.commandLater(command)

    @throws(classOf[CompilerException])
    @throws(classOf[LogoException])
    final def report(source: String): AnyRef = {
        try {
            return comp.report(source)
        } catch {
            case compilerException: CompilerException => throw compilerException
            case logoException: LogoException => throw logoException
        }
    }

    /**
    Report by using handler to catch either the result or the error
    Returns a value from the code provided
    */
    final def reportAndCallback(code: String, 
        resultHandler: (AnyRef) => Unit,
        errorHandler: (CompilerException) => Unit = (errorHandler) => errorHandler.printStackTrace
        ): Unit = {
        comp.reportAndCallback(code,  new InvocationListener(){
            def handleResult(value: AnyRef) = resultHandler(value)
            def handleError(compilerException: CompilerException) = errorHandler(compilerException)
        })
    } 




    val netlogoThread = new Thread {
        override def run {
            java.awt.EventQueue.invokeAndWait(
                new Runnable {
                    override def run {
                        frame.add(comp)
                        frame.setSize(netlogoModel.width, netlogoModel.height)
                        frame.setTitle(netlogoModel.title.getOrElse("My simple netlogo model"))
                        frame.setVisible( true )

                        netlogoModel.pos.foreach(p => frame.setLocation(new Point(p._1 , p._2)) )
                            
                        try {
                            comp.open(netlogoModel.src)
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

        comp.listenerManager.addListener(new NetLogoListener{
            override def  tickCounterChanged(ticksCounter: Double) = {
              if(ticks < netlogoModel.maxTicks.toDouble) {
                ticks = ticksCounter.toInt
                check
              }
            }
            override def buttonPressed(buttonName: String): Unit = {}                                                                                                                                                                                                      
            override def buttonStopped(buttonName: String): Unit = {}                                                                                                                                                                                                      
            override def chooserChanged(name: String, value: AnyRef, valueChanged: Boolean): Unit = {}                                                                                                                                                                     
            override def codeTabCompiled(text: String, errorMsg: CompilerException): Unit = {}                                                                                                                                                              
            override def commandEntered(owner: String, text: String, agentType: Char, errorMsg: CompilerException): Unit = {}                                                                                                                               
            override def inputBoxChanged(name: String, value: AnyRef, valueChanged: Boolean): Unit = {}                                                                                                                                                                    
            override def modelOpened(name: String): Unit = {}                                                                                                                                                                                                
            override def possibleViewUpdate(): Unit = {}          
            //TODO: change those two                                                                                                                                                                                                         
            override def sliderChanged(name: String, value: Double, min: Double, increment: Double, max: Double, valueChanged: Boolean, buttonReleased: Boolean): Unit = {}                                                                                                
            override def switchChanged(name: String, value: Boolean, valueChanged: Boolean): Unit = {}            
        })


        pollingLoopThread.join
        netlogoThread.join
        logger.info(s"the polling loop thread has finished")
        logger.info(s"the netlogo thread has finished")

    }
}