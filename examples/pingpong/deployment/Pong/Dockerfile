FROM openjdk:17-oraclelinux7

ARG SBT_VERSION=1.6.2
ARG SCALA_VERSION=2.13.8
ARG PROJECT_VERSION=0.1.0-SNAPSHOT
ENV BROKER=localhost:9092

ENV SCALA_VERSION=${SCALA_VERSION}
ENV PROJECT_VERSION=${PROJECT_VERSION}


ENV BASIC_USER=basic_user
ENV BASIC_USER_HOME=/${BASIC_USER}


RUN useradd -m -u 1000 -d ${BASIC_USER_HOME} ${BASIC_USER}
RUN yum -y install unzip 
RUN yum -y install git


USER ${BASIC_USER}
WORKDIR ${BASIC_USER_HOME}


# Install sbt
RUN curl -L -o sbt-$SBT_VERSION.zip https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.zip
RUN unzip sbt-$SBT_VERSION.zip -d $BASIC_USER_HOME/ops
USER  root
RUN ln -s $BASIC_USER_HOME/ops/sbt/bin/sbt /usr/bin/sbt
USER ${BASIC_USER}


# get project

RUN git clone https://github.com/jeromeCondere/matete.git

WORKDIR matete


SHELL   [ "sbt" ]

RUN pingpong / Pong /  assembly
RUN  exit
WORKDIR examples/pingpong/target/scala-2.13

SHELL   ["/bin/bash", "-c" ]

ENTRYPOINT  java -jar  PongApp-${PROJECT_VERSION}_${SCALA_VERSION}.jar ${BROKER}

EXPOSE 9092
EXPOSE 80