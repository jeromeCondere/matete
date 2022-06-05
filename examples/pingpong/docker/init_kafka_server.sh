are_brokers_available() {
    expected_num_brokers=1 # replace this with a proper value
    num_brokers=$(("$(zookeeper-shell.sh zookeeper:2181 ls /brokers/ids 2>/dev/null | grep -o , | wc -l)" + 1))
    return $((num_brokers >= expected_num_brokers))
}

while ! are_brokers_available; do
    echo "brokers not available yet"
    sleep 1
done

echo "Kafka custom init"

/opt/bitnami/kafka/bin/kafka-topics.sh --create  --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic Ping-topic
/opt/bitnami/kafka/bin/kafka-topics.sh  --list --bootstrap-server localhost:9092

echo "Kafka custom init done"