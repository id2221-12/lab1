#!/bin/bash
$HADOOP_HOME/bin/hadoop jar hbaseMapreduce.jar hbase.HBaseMapReduce
$HBASE_HOME/bin/hbase shell < check.hbase
