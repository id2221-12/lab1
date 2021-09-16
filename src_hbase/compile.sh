#!/bin/bash

$HBASE_HOME/bin/start-hbase.sh && sleep 5 && $HBASE_HOME/bin/hbase shell hbase.hbase && sleep 5
export HADOOP_CLASSPATH=$($HADOOP_HOME/bin/hadoop classpath)
export HBASE_CLASSPATH=$($HBASE_HOME/bin/hbase classpath)
export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:$HBASE_CLASSPATH
javac -cp $HADOOP_CLASSPATH -d hbase_classes hbase/HBaseMapReduce.java
jar -cvf hbaseMapreduce.jar -C hbase_classes/ .
