package mas.examples.pingpong
import com.matete.mas.agent.Agent
import com.matete.mas.agent.AgentId
import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.consumer._
import  java.util

object  MonitorApp extends App{
    val monitor = new Monitor
    monitor.run
  
}

class Monitor { 


 def run = {

    val  props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    
    props.put("group.id", "myGroup")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer")

    val consumerString = new KafkaConsumer[String, String](props)
    consumerString.subscribe(util.Collections.singletonList("Ping-topic"))
    consumerString.subscribe(util.Collections.singletonList("Pong-topic"))
    try{
        while(true){
            val records = consumerString.poll(100)
            records.forEach{ record =>
                println(s"topic = ${record.topic()}, partition = ${record.partition()}, offset = ${record.offset()}, key = ${record.key()}, value = ${record.value()}\n")
            }      
        }
    } finally {
        consumerString.close()
    }
 }
}