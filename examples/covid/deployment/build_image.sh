docker run --rm --name postgres -p 5432:5432 \
-e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
-e POSTGRES_DB=db \
-e POSTGRES_HOST_AUTH_METHOD=md5 \
-d postgres



docker exec -it postgres psql -d db -U postgres


docker run --name covid \
-v /tmp/.X11-unix/:/tmp/.X11-unix/ \
-e BROKER="host.docker.internal:9092" \
-e POSTGRES_HOST="postgres:5432" \
-e DISPLAY="$DISPLAY" \
--add-host "host.docker.internal:host-gateway" \
jeromecondere/covid



docker run --name ckt  -ti --rm -e DISPLAY="host.docker.internal:0" \
--privileged \
--volume="$HOME/.Xauthority:/root/.Xauthority:rw" \
--add-host "host.docker.internal:host-gateway" \
jeromecondere/mmm


docker run --name ckt  -it --rm -e DISPLAY="host.docker.internal:host-gateway$DISPLAY" \
--privileged \
--volume="$HOME/.Xauthority:/root/.Xauthority:rw" \
--add-host "host.docker.internal:host-gateway" \
jeromecondere/mmm


docker run --name ckt  -it --rm -e DISPLAY="$DISPLAY" \
-v /tmp/.X11-unix/:/tmp/.X11-unix/ \
--volume="$HOME/.Xauthority:/root/.Xauthority:rw" \
--add-host "host.docker.internal:host-gateway" \
jeromecondere/mmm




java -jar CovidApp-0.1.0-SNAPSHOT_2.12.15.jar localhost:9092 localhost:5432 /home/alpha-omega/Bureau/kafka_projects/matete_MAS/examples/covid/src/resources/epiDEM_Basic.nlogo
java -Djava.awt.headless=true  -jar CovidHeadlessApp-0.1.0-SNAPSHOT_2.12.15.jar localhost:9092 localhost:5432 /home/alpha-omega/Bureau/kafka_projects/matete_MAS/examples/covid/src/resources/epiDEM_Basic.nlogo

java  -cp "netlogo-6.2.0.jar"  -jar CovidApp-0.1.0-SNAPSHOT_2.12.15.jar localhost:9092 localhost:5432 /home/alpha-omega/Bureau/kafka_projects/matete_MAS/examples/covid/src/resources/epiDEM_Basic.nlogo





kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic uk-topic
kafka-topics.sh --bootstrap-server localhost:9092 --delete  --topic france-topic
kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic falconia-topic
kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic ServerManager-topic

kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic uk-cobalt-topic
kafka-topics.sh --bootstrap-server localhost:9092 --delete  --topic france-cobalt-topic
kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic falconia-cobalt-topic


kafka-topics.sh --bootstrap-server localhost:9092 --list




kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic 'uk-.*'
kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic 'france-.*'
kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic 'falconia-.*'
kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic ServerManager-topic


falconia-kovwaltkoi-cobalt-topic
docker stop $(docker ps -a -q)

docker run --rm --name postgres -p 5432:5432 \
-e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
-e POSTGRES_DB=db \
-e POSTGRES_HOST_AUTH_METHOD=md5 \
-d postgres



curl -H "Content-Type: application/json" -XPOST -d '{"id": "cobalt", "name": "kovwaltkoi", "description": null, "parameters": {"iok": 545, "ujl": "mfejo"}}' localhost:7070/experiment



curl -H "Content-Type: application/json" -XPOST -d '{"id": "cobalt", "name": "Covid cobalt", "description": null, "parameters": {"recorveryChanceEpsilon": 0.0, "infectionChanceEpsilon": 0.0, "repeat": 3}}' localhost:7070/experiment
curl -H "Content-Type: application/json" -XPOST -d '{"id": "cobalt-headless", "name": "Covid cobalt", "description": null, "parameters": {"recorveryChanceEpsilon": 0.0, "infectionChanceEpsilon": 0.0, "repeat": 10}}' localhost:7070/experiment



curl -H "Content-Type: application/json" -XPOST -d '{"id": "cobalt-headless", "name": "Covid cobalt", "description": null, "parameters": {"recorveryChanceEpsilon": 0.0, "infectionChanceEpsilon": 0.0, "repeat": 0}}' localhost:7070/experiment


select * from event_covid where experiment_id = 'cobalt-repeat-1' and country='france' order by ticks asc;




docker exec -it covid_experiment /bin/bash



curl -H "Content-Type: application/json" -XPOST -d '{"id": "cobalt-headless", "name": "Covid cobalt yak", "description": null, "parameters": {"recorveryChanceEpsilon": 0.0, "infectionChanceEpsilon": 0.0, "repeat": 0}}' http://covid.experiment.local/api/covid-simple/experiment



docker run  --name covid-headless \
-e BROKER="host.docker.internal:9092" \
-e POSTGRES_HOST="postgres:5432" \
-e BASIC_USER_GROUP=1047 \
-e BASIC_USER_ID=7000 \
--add-host "host.docker.internal:host-gateway" \
jeromecondere/covid-headless:1.0