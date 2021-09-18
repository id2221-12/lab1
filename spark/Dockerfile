FROM ubuntu:latest

RUN apt-get update && apt-get install -y bash ssh vim openjdk-8-jre-headless openjdk-8-jdk-headless
RUN wget -qO- https://archive.apache.org/dist/spark/spark-2.4.3/spark-2.4.3-bin-hadoop2.7.tgz | tar -zxf - -C /root/

ENV JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/"
ENV SPARK_HOME="/root/spark-2.4.3-bin-hadoop2.7/"
ENV PATH=$SPARK_HOME/bin:$JAVA_HOME/bin:$PATH
COPY pagecounts-20160101-000000_parsed.out /root/
COPY main.scala /root/

WORKDIR /root/
ENTRYPOINT ["/bin/bash"]
