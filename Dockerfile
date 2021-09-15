FROM ubuntu:latest

RUN apt-get update && apt-get install -y bash openssh-server openssh-client rsync vim openjdk-8-jre-headless openjdk-8-jdk-headless
RUN wget -qO- https://dlcdn.apache.org/hadoop/common/hadoop-3.2.2/hadoop-3.2.2.tar.gz | tar -zxf - -C /root/ 

ENV JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/"
ENV HADOOP_HOME="/root/hadoop-3.2.2"
ENV HADOOP_CONFIG="$HADOOP_HOME/etc/hadoop"
ENV PATH=$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH

RUN echo 'JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/"' >> $HADOOP_CONFIG/hadoop-env.sh && mkdir -p $HADOOP_HOME/hdfs/namenode && mkdir -p $HADOOP_HOME/hdfs/datanode

COPY core-site.xml $HADOOP_CONFIG/core-site.xml
COPY hdfs-site.xml $HADOOP_CONFIG/hdfs-site.xml
COPY start_daemon.sh /root/

RUN chmod +x /root/start_daemon.sh
RUN $HADOOP_HOME/bin/hdfs namenode -format


ENTRYPOINT ["/bin/bash"]
