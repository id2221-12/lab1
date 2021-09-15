#!/bin/bash
$HADOOP_HOME/bin/hadoop jar wordcount.jar wordcount.WordCount input output
$HADOOP_HOME/bin/hdfs dfs -ls output
$HADOOP_HOME/bin/hdfs dfs -cat output/part-r-00000
