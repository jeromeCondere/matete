#!/bin/bash

set +x

. /opt/bitnami/scripts/libkafka.sh

# Load Kafka environment
. /opt/bitnami/scripts/kafka-env.sh

KAFKA_HOME=/opt/bitnami/kafka
KAFKA_CONF_DIR=/opt/bitnami/kafka/config
echo $KAFKA_HOME

kafka_start_bg() {
    if [[ "${KAFKA_CFG_LISTENERS:-}" =~ SASL ]] || [[ "${KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP:-}" =~ SASL ]]; then
        export KAFKA_OPTS="-Djava.security.auth.login.config=$KAFKA_HOME/conf/kafka_jaas.conf"
        echo "yo"
    fi
    local flags=("$KAFKA_CONF_DIR/server.properties")
    [[ -z "${KAFKA_EXTRA_FLAGS:-}" ]] || flags=("${flags[@]}" "${KAFKA_EXTRA_FLAGS[@]}")
    echo "$flags"
    # echo $KAFKA_EXTRA_FLAGS
    local start_command="$KAFKA_HOME/bin/kafka-server-start.sh -daemon $flags"
    echo "feoro"
    echo $start_command

    info "Starting Kafka in background"
    # am_i_root && start_command=("gosu" "${KAFKA_DAEMON_USER}" "${start_command[@]}")
    if [[ "${BITNAMI_DEBUG:-false}" = true ]]; then
        eval "$start_command"
    else
        eval "$start_command" >/dev/null 2>&1 &
    fi
    ps -aux | grep kafka
    # Wait for Kakfa to start
    local counter=0
    while (( counter < 60 )); do
        echo "hmm"
        if ( cat < /dev/null > /dev/tcp/localhost/"9092" ) 2>/dev/null; then
            echo "hmm 3"
            break
        fi
        echo "kafka not yet started"
        sleep 1
        echo "pff"
        ((counter++))
    done
}

kafka_stop() {
    info "Stopping Kafka"
    local stop_command=("$KAFKA_HOME/bin/kafka-server-stop.sh")
    am_i_root && stop_command=("gosu" "${KAFKA_DAEMON_USER}" "${stop_command[@]}")
    if [[ "${BITNAMI_DEBUG:-false}" = true ]]; then
        "${stop_command[@]}"
    else
        "${stop_command[@]}" >/dev/null 2>&1
    fi
}

are_brokers_available() {
    expected_num_brokers=3 # replace this with a proper value
    num_brokers=$(("$(zookeeper-shell.sh zookeeper:2181 ls /brokers/ids 2>/dev/null | grep -o , | wc -l)" + 1))
    (( num_brokers >= expected_num_brokers ))
}

echo "Kafka custom init" 

kafka_start_bg

while ! are_brokers_available; do
    echo "brokers not available yet"
    sleep 1
done

/opt/bitnami/kafka/bin/kafka-topics.sh --create  --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic Ping-topic
/opt/bitnami/kafka/bin/kafka-topics.sh  --list --bootstrap-server localhost:9092

kafka_stop

echo "Kafka custom init done" 