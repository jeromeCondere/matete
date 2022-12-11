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

    val create_event_table  = """CREATE TABLE IF NOT EXISTS event_covid(
       agent_id varchar(80),
       experiment_id  varchar(50),
       country varchar(80),
       infected_count bigint,
       not_infected_count bigint,
       travellers bigint,
       ticks double precision
    );"""

    val create_experiment_id  = """CREATE TABLE IF NOT EXISTS experiment(
       experiment_id  varchar(50),
       description text
    );"""
      
// select ('${agentMessage.agentId.id}','${covidMessage.experimentId}','${modelTrend.country}', ${modelTrend.infectedCount}, ${modelTrend.notInfectedCount}, ${modelTrend.travellers}, ${modelTrend.ticks});

    val conn = DriverManager.getConnection(con_str)
    try {
        val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        val rs = stm.execute(create_event_table)

        val stm2 = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        val rs2 = stm.execute(create_experiment_id)
    } finally {
     conn.close()
    }

    override def receive(agentMessages: List[AgentMessage[CovidMessage]], consumerName: String) = {
        val insertQuery = agentMessages.map( agentMessage => {
            val covidMessage = agentMessage.message
            val modelTrend =  covidMessage.modelBehavior.get
            // insert to avoid duplicates

            s""" 
                insert into event_covid(agent_id, experiment_id, country, infected_count, not_infected_count, travellers, ticks)
                select '${agentMessage.agentId.id}','${covidMessage.experimentId}','${modelTrend.country}', ${modelTrend.infectedCount}, ${modelTrend.notInfectedCount}, ${modelTrend.travellers}, ${modelTrend.ticks}
                WHERE
                NOT EXISTS (
                    SELECT agent_id, experiment_id, country, infected_count, not_infected_count, travellers, ticks FROM event_covid
                     WHERE agent_id = '${agentMessage.agentId.id}' and 
                     experiment_id ='${covidMessage.experimentId}' and 
                     country ='${modelTrend.country}' and 
                     infected_count = ${modelTrend.infectedCount} and 
                     not_infected_count= ${modelTrend.notInfectedCount} and 
                     travellers = ${modelTrend.travellers} and 
                     ticks = ${modelTrend.ticks}
                );
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