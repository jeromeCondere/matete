FROM openjdk:11

ARG SBT_VERSION=1.6.2
ARG SCALA_VERSION=2.13.8
ARG PROJECT_VERSION=0.1.0-SNAPSHOT
ENV BROKER=localhost:9092

ENV SCALA_VERSION=${SCALA_VERSION}
ENV PROJECT_VERSION=${PROJECT_VERSION}

# Install sbt
RUN curl -L -o sbt-$SBT_VERSION.zip https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.zip
RUN unzip sbt-$SBT_VERSION.zip -d ops


RUN curl -fL https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz | gzip -d > cs && chmod +x cs
RUN yes | ./cs setup
RUN ./cs install scala:$SCALA_VERSION && ./cs install scalac:$SCALA_VERSION


RUN git clone https://github.com/jeromeCondere/matete.git

WORKDIR matete
# RUN scala
SHELL   ["/bin/bash" , "/ops/sbt/bin/sbt" ]

RUN pingpong / Pong /  assembly
RUN  exit
WORKDIR examples/pingpong/target/scala-2.13

SHELL   ["/bin/bash", "-c" ]

ENTRYPOINT  /./cs launch scala:$SCALA_VERSION  -- PongApp-${PROJECT_VERSION}_${SCALA_VERSION}.jar ${BROKER}


EXPOSE 9092

