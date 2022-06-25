# Ping Pong agent system

## What does it do?
The goal of the pingpong agent system is to make two agents communicate with each other, the ping agent start by sending ping to the pong agent, that responds to all ping message received by a pong message.

## How do I deploy it
For docker use:  
`` docker-compose  -f examples/pingpong/deployment/docker-compose-ping-pong_bitnami-kafka.yaml up ``
For k8s use:  
`` helm install simple-project .  -f path/to/pingpong-values-example.yaml ``
