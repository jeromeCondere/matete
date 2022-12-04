// package com.matete.mas.agent.simulation.netlogo



// case class State(value: Int) {

//   def flatMap(f: Int => State): State = {
//     println(">>>>>>>  "+ this.toString + " flatmap " + "\n")
//     val newState = f(value)
//     println("<<<<<<<  " + this.toString + " flatmap \n")
//     State(newState.value)
//   }

//   def map(f: Int => Int) = {
//     println(">>>>>>>  "+ this.toString + " map " + "\n")
//     val res = State(f(value))
//     println("<<<<<<<  " + this.toString + " map \n")
//     res
//   }
// }

//     val res3 = for {
//       a <- State(20)
//       b <- State(a + 15) //manually carry over `a`
//       c <- State(b + 0) //manually carry over `b`
//     } yield c
//     println(s"res: $res3") //prints "State(35)"

//     //val res3 = State(20).flatMap(((a) => State(a.$plus(15)).
//     // flatMap(((b) => State(b.$plus(0)).map(((c) => c))))));

//   }

// LaterReporter

// laterReporter("count turtles with [ infected? ]").
// fM(
// 	a =>  LaterReporter("count turtles with [ not infected? ]").fM(
// 		b => LaterReporter("count turtles with [ travel? ]").fM(
// 			c => LaterReporter("ticks").map(
// 				d  => covid(a,b,c,d)
// 			)
// 		)

// 	)
// )

// for {

// 	a <- LaterReporter("count turtles with [ infected? ]")
// 	b <- LaterReporter("count turtles with [ not infected? ]")
// 	c <- LaterReporter("count turtles with [ travel? ]")
// 	d <- LaterReporter("ticks")
// } yield CovidMessage(None,  Some(CovidModelBehaviour(
//                             infectedCount = a,
//                             notInfectedCount = b,
//                             country = modelConfig.name,
//                             travellers = c,
//                             ticks = d
//                         )
//                     ))

// LaterReporter("count turtles with [ not infected? ]").fM = reportAndCallback(cmd, x => f(x))

// case class LaterReporter(command: String, acc) {

// 	def flatMap(f: String => LaterReporter): LaterReporter = {

// 	   reportAndCallback(command, x => f
// 	}
// }
// }

// //TODO create report monad
// reportAndCallback("count turtles with [ infected? ]", 
//     infectedCount => reportAndCallback("count turtles with [ not infected? ]", notInfectedCount => 
//         reportAndCallback("count turtles with [ travel? ]", travellers => 
//             reportAndCallback("ticks", ticks =>  send(
//                     AgentId("ServerManager"), 
//                     CovidMessage(None,  Some(CovidModelBehaviour(
//                             infectedCount = infectedCount.asInstanceOf[Double].toInt,
//                             notInfectedCount = notInfectedCount.asInstanceOf[Double].toInt,
//                             country = modelConfig.name,
//                             travellers = travellers.asInstanceOf[Double].toInt,
//                             ticks = ticks.asInstanceOf[Double]
//                         )
//                     ))
//                 )
//             )
//         )
//                 