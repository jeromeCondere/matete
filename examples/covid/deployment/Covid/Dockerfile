FROM  centos:latest 

ARG SBT_VERSION=1.6.2
ARG SCALA_VERSION=2.12.15
ARG PROJECT_VERSION=0.1.0-SNAPSHOT
ENV BROKER=localhost:9092
ENV POSTGRES_HOST=host.docker.internal:5432
ENV MODEL=epiDEM_Basic.nlogo
ENV SCALA_VERSION=${SCALA_VERSION}
ENV PROJECT_VERSION=${PROJECT_VERSION}


ENV BASIC_USER=basic_user
ENV BASIC_USER_HOME=/${BASIC_USER}


RUN useradd -m -u 1000 -d ${BASIC_USER_HOME} ${BASIC_USER}

RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-*
RUN sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*


RUN yum update -y
RUN yum -y install unzip git
RUN yum install -y java-17-openjdk
RUN  yum install -y dbus-x11 PackageKit-gtk3-module libcanberra-gtk2
RUN  yum install -y glibc-locale-source 

USER ${BASIC_USER}
WORKDIR ${BASIC_USER_HOME}


# Install sbt
RUN curl -L -o sbt-$SBT_VERSION.zip https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.zip
RUN unzip sbt-$SBT_VERSION.zip -d $BASIC_USER_HOME/ops
USER  root
RUN ln -s $BASIC_USER_HOME/ops/sbt/bin/sbt /usr/bin/sbt


# get project

USER ${BASIC_USER}
RUN git clone https://github.com/jeromeCondere/matete.git

USER root
RUN mkdir /data
RUN cp ${BASIC_USER_HOME}/matete/examples/covid/src/resources/${MODEL} /data/
RUN chown -R ${BASIC_USER} /data/

WORKDIR matete


SHELL   [ "sbt" ]

RUN covid / CovidConfig /  assembly
RUN  exit
WORKDIR examples/covid/target/scala-2.12

SHELL   ["/bin/bash", "-c" ]

ENTRYPOINT  java  -jar  CovidApp-${PROJECT_VERSION}_${SCALA_VERSION}.jar ${BROKER} ${POSTGRES_HOST} /data/${MODEL}

EXPOSE 9092
EXPOSE 80