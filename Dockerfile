FROM ubuntu:latest

RUN apt-get update && apt-get install -y bash ssh rsync vim openjdk-8-jre-headless openjdk-8-jdk-headless
RUN wget -qO- https://archive.apache.org/dist/hadoop/common/hadoop-3.1.2/hadoop-3.1.2.tar.gz | tar -zxf - -C /root/ 
RUN wget -qO- https://archive.apache.org/dist/hbase/1.4.10/hbase-1.4.10-bin.tar.gz | tar -zxf - -C /root/ 

RUN mkdir /root/.ssh && ssh-keygen -b 2048 -t rsa -f /root/.ssh/id_rsa -q -N "" && cp /root/.ssh/id_rsa.pub /root/.ssh/authorized_keys

ENV JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/"
ENV HADOOP_HOME="/root/hadoop-3.1.2"
ENV HADOOP_CONFIG="$HADOOP_HOME/etc/hadoop"
ENV HBASE_HOME="/root/hbase-1.4.10"
ENV HBASE_CONF="$HBASE_HOME/conf/"
ENV PATH=$HBASE_HOME/bin:$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH

RUN echo 'export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/"' >> $HADOOP_CONFIG/hadoop-env.sh && mkdir -p $HADOOP_HOME/hdfs/namenode && mkdir -p $HADOOP_HOME/hdfs/datanode
RUN echo 'export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/"' >> $HBASE_CONF/hbase-env.sh && mkdir -p $HBASE_HOME/zookeeper

COPY core-site.xml $HADOOP_CONFIG/core-site.xml
COPY hdfs-site.xml $HADOOP_CONFIG/hdfs-site.xml
COPY hbase-site.xml $HBASE_CONF/hbase-site.xml
COPY start_daemon.sh /root/
COPY src_hbase /root/src_hbase/
COPY src_hadoop /root/src_hadoop/

RUN chmod +x /root/start_daemon.sh
RUN $HADOOP_HOME/bin/hdfs namenode -format

WORKDIR /root/
ENTRYPOINT ["/bin/bash"]
