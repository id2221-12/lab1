#!/bin/bash

$HADOOP_HOME/bin/hdfs dfs -mkdir -p input
$HADOOP_HOME/bin/hdfs dfs -put file0 input/file0
$HADOOP_HOME/bin/hdfs dfs -put file1 input/file1
$HADOOP_HOME/bin/hdfs dfs -ls input
export HADOOP_CLASSPATH=$($HADOOP_HOME/bin/hadoop classpath)
javac -cp $HADOOP_CLASSPATH -d wordcount_classes wordcount/WordCount.java
jar -cvf wordcount.jar -C wordcount_classes/ .
