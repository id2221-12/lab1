FROM ubuntu:latest

RUN apt-get update && apt-get install -y bash ssh vim openjdk-8-jre-headless openjdk-8-jdk-headless
RUN wget -qO- https://archive.apache.org/dist/spark/spark-2.4.3/spark-2.4.3-bin-hadoop2.7.tgz | tar -zxf - -C /root/
RUN wget -qO- https://archive.apache.org/dist/kafka/2.0.0/kafka_2.11-2.0.0.tgz | tar -zxf - -C /root/
RUN wget -qO- https://github.com/sbt/sbt/releases/download/v1.5.5/sbt-1.5.5.tgz | tar -zxf - -C /root/

ENV JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/"
ENV SPARK_HOME="/root/spark-2.4.3-bin-hadoop2.7/"
ENV KAFKA_HOME="/root/kafka_2.11-2.0.0/"
ENV SBT="/root/sbt/"
ENV PATH=$SBT/bin:$KAFKA_HOME/bin:$SPARK_HOME/bin:$JAVA_HOME/bin:$PATH
ADD ./lab2/src /root/src 

WORKDIR /root/
ENTRYPOINT ["/bin/bash"]
