FROM openjdk:17-oraclelinux7

ARG SBT_VERSION=1.6.2
ARG SCALA_VERSION=2.12.15
ARG SCALA_SHORT_VERSION=2.12
ARG PROJECT_VERSION=0.1.0-SNAPSHOT


ENV BROKER_PORT=9092
ENV BROKER=localhost:${BROKER_PORT}
ENV SERVER_API_HOST=0.0.0.0
ENV SERVER_API_PORT=7070
ENV POSTGRES_PORT=5432
ENV POSTGRES_HOST=host.docker.internal
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV POSTGRES_DB=db
ENV MODEL=epiDEM_Basic.nlogo
ENV SCALA_VERSION=${SCALA_VERSION}
ENV PROJECT_VERSION=${PROJECT_VERSION}


ENV BASIC_USER_GROUP=1001
ENV BASIC_USER_ID=1001
ENV BASIC_USER=matete
ENV BASIC_USER_GROUP_NAME=matete
ENV BASIC_USER_HOME=/${BASIC_USER}


USER root

RUN yum update -y && yum -y install unzip git

RUN mkdir ${BASIC_USER_HOME}

WORKDIR ${BASIC_USER_HOME}


# Install sbt
RUN curl -L -o sbt-$SBT_VERSION.zip https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.zip \
  && unzip sbt-$SBT_VERSION.zip -d $BASIC_USER_HOME/ops \
&& ln -s $BASIC_USER_HOME/ops/sbt/bin/sbt /usr/bin/sbt


# get project

RUN git clone https://github.com/jeromeCondere/matete.git

RUN mkdir /data
RUN cp ${BASIC_USER_HOME}/matete/examples/covid/src/resources/${MODEL} /data/

WORKDIR matete


SHELL   [ "sbt" ]

RUN covid / CovidHeadlessConfig /  assembly
RUN  exit

WORKDIR examples/covid/target/scala-${SCALA_SHORT_VERSION}
SHELL   ["/bin/bash", "-c" ]


COPY entrypoint.sh .


RUN chmod 755 entrypoint.sh && groupadd -g ${BASIC_USER_GROUP} ${BASIC_USER_GROUP_NAME} \
&& useradd -m -u ${BASIC_USER_ID} -d ${BASIC_USER_HOME} -g ${BASIC_USER_GROUP_NAME} ${BASIC_USER} \
&& chown -R ${BASIC_USER}:${BASIC_USER_GROUP} /data/ \
&& chmod -R 755 /data \
&& chown -R ${BASIC_USER}:${BASIC_USER_GROUP} ${BASIC_USER_HOME}/matete/examples/covid/target/scala-${SCALA_SHORT_VERSION}/  \
&& chmod -R 755 ${BASIC_USER_HOME}/matete/examples/covid/target/scala-${SCALA_SHORT_VERSION}/ 




USER $BASIC_USER

ENTRYPOINT  sh entrypoint.sh

#USER $BASIC_USER
# ENTRYPOINT  /bin/bash
EXPOSE ${SERVER_API_PORT}
EXPOSE 80