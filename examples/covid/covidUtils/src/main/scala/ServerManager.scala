package com.matete.examples.covid

import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import com.matete.mas.agent.AgentMessage
import com.matete.mas.configuration.DefaultConfig.defaultConfig
import java.sql.{Connection, DriverManager, ResultSet}


class ServerManager(brokers: List[String], host: String = "localhost") extends Agent[CovidMessage](defaultConfig(brokers = brokers, agentId = AgentId("ServerManager")))(
        Some("com.matete.examples.covid.AgentMessageCovidMessageSerializer"),
        Some("com.matete.examples.covid.AgentMessageCovidMessageDeserializer")
     ) with Runnable{


    classOf[org.postgresql.Driver]
    val DBUser = "postgres"
    val DBName = "db"
    val pwd = "postgres"



    val con_str = s"jdbc:postgresql://$host/$DBName?user=$DBUser&password=$pwd"

    val create_experiment_table  = """CREATE TABLE IF NOT EXISTS event_covid(
       experiment_no  varchar(50),
       country varchar(80),
       infected_count bigint,
       not_infected_count bigint,
       travellers bigint,
       ticks double precision
    );"""
   

    val conn = DriverManager.getConnection(con_str)
    try {
        val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        val rs = stm.execute(create_experiment_table)
    } finally {
     conn.close()
    }

    override def receive(agentMessages: List[AgentMessage[CovidMessage]], consumerName: String) = {
        val insertQuery = agentMessages.map( agentMessage => {
            val modelTrend =  agentMessage.message.modelBehavior.get
            s""" 
                insert into event_covid(experiment_no, country, infected_count, not_infected_count, travellers, ticks)
                values ('experiment_no1','${modelTrend.country}', ${modelTrend.infectedCount}, ${modelTrend.notInfectedCount}, ${modelTrend.travellers}, ${modelTrend.ticks});
            """
        }).mkString("\n")

        val conn = DriverManager.getConnection(con_str)
        try {
            val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
            val rs = stm.executeUpdate(insertQuery)

        } finally {
         conn.close()
        }
    }
}