#!/bin/bash

cd "$(dirname "$0")"

# Auto-create classpath
CLASSPATH=""
for lib in ../lib/*.jar;
do
  CLASSPATH="${CLASSPATH}:$lib"
done

java -Xmx256m -cp "${CLASSPATH}" net.timewalker.ffmq4.FFMQAdminClientLauncher $*
