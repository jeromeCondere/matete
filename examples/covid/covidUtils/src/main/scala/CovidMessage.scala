package com.matete.examples.covid


case class CovidTurtle(
	cured: Boolean,
	infected: Boolean,
	susceptible: Boolean,
	country: String,
	recoveryTime: Double,
	infectionLength: Double,
	nbInfected: Double,
	nbRecovered: Double
)

case class CovidModelBehaviour(infectedCount: Int, notInfectedCount: Int, country: String, travellers: Int, ticks: Double)
case class CovidMessage(
	turtles: Option[List[CovidTurtle]] = None,
	modelBehavior: Option[CovidModelBehaviour] = None
)