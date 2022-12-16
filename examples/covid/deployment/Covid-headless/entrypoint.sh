# entrypoint.sh
#!/usr/bin/env bash

set -e

echo "Running covid  headless example"



exec  java -Djava.awt.headless=true   -jar  CovidHeadlessApp-${PROJECT_VERSION}_${SCALA_VERSION}.jar ${BROKER} ${POSTGRES_HOST}:${POSTGRES_PORT} ${SERVER_API_HOST} ${SERVER_API_PORT} ${POSTGRES_USER} ${POSTGRES_PASSWORD} ${POSTGRES_DB} /data/${MODEL}

#create user temp, use it  do everything