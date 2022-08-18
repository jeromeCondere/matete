package com.matete.mas.agent.simulation.netlogo



final case class NetlogoModel(
    src: String,
    maxTicks: Int = NetlogoConstants.DEFAULT_MAX_TICKS,
    width: Int = NetlogoConstants.WINDOWS_DEFAULT_WIDTH,
    height: Int = NetlogoConstants.WINDOWS_DEFAULT_HEIGHT,
    pos: Option[(Int,Int)] = None,
    name: Option[String] = None,
    title: Option[String] = None
)