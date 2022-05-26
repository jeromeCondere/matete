FROM openjdk:11

ENV SBT_VERSION=1.6.2
ENV SCALA_VERSION=2.13.8

RUN echo $SBT_VERSION



# Install sbt
RUN curl -L -o sbt-$SBT_VERSION.zip https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.zip
RUN unzip sbt-$SBT_VERSION.zip -d ops
# RUN export /ops/sbt/bin/sbt

RUN curl -fL https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz | gzip -d > cs && chmod +x cs
RUN yes | ./cs setup
RUN ./cs install scala:$SCALA_VERSION && ./cs install scalac:$SCALA_VERSION
RUN echo "alias scala='./cs launch scala:$SCALA_VERSION'" >>/root/.bashrc
RUN echo "alias sbt='/ops/sbt/bin/sbt'" >>/root/.bashrc


RUN git clone https://github.com/jeromeCondere/matete.git

WORKDIR matete
# RUN sbt 
# RUN  pingpong / Pong /  assembly
# RUN scala  examples/pingpong/


EXPOSE 9092

ENTRYPOINT [ "/bin/bash" ]