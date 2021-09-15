#!/bin/bash

/etc/init.d/ssh start
hdfs --daemon start namenode
hdfs --daemon start datanode
