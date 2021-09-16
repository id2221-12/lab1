#!/bin/bash
export HADOOP_CLASSPATH=$($HADOOP_HOME/bin/hadoop classpath)
export HBASE_CLASSPATH=$($HBASE_HOME/bin/hbase classpath)
export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:$HBASE_CLASSPATH
$HADOOP_HOME/bin/hadoop jar hbaseMapreduce.jar hbase.HBaseMapReduce
$HBASE_HOME/bin/hbase shell < check.hbase
