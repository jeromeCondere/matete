docker run --rm --name postgres -p 5432:5432 \
-e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
-e POSTGRES_DB=db \
-e POSTGRES_HOST_AUTH_METHOD=md5 \
-d postgres



docker exec -it postgresql psql -d db -U postgres


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




java -Djava.awt.headless=true  -jar CovidApp-0.1.0-SNAPSHOT_2.12.15.jar localhost:9092 localhost:5432 /home/alpha-omega/Bureau/kafka_projects/matete_MAS/examples/covid/src/resources/epiDEM_Basic.nlogo

java  -cp "netlogo-6.2.0.jar"  -jar CovidApp-0.1.0-SNAPSHOT_2.12.15.jar localhost:9092 localhost:5432 /home/alpha-omega/Bureau/kafka_projects/matete_MAS/examples/covid/src/resources/epiDEM_Basic.nlogo






docker stop $(docker ps -a -q)