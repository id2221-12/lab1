#!/bin/bash

$HBASE_HOME/bin/start-hbase.sh
$HBASE_HOME/bin/hbase shell hbase.hbase
export HADOOP_CLASSPATH=$($HADOOP_HOME/bin/hadoop classpath)
export HBASE_CLASSPATH=$($HBASE_HOME/bin/hbase classpath)
export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:$HBASE_CLASSPATH
javac -cp `hbase classpath`:/root/hbase-2.3.6/lib/hbase-mapreduce-2.3.6.jar -d hbase_classes hbase/HBaseMapReduce.java
jar -cvf hbaseMapreduce.jar -C hbase_classes/ .
