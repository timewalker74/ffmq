#!/bin/bash

cd "$(dirname "$0")"

# Get application home
[ -z "$FFMQ_HOME" ] && FFMQ_HOME=$(cd .. ; pwd)

# Get application base
[ -z "$FFMQ_BASE" ] && FFMQ_BASE=$FFMQ_HOME

# Look for an optional setenv.sh file
if [ -r "$FFMQ_BASE"/bin/setenv.sh ]; then
  . "$FFMQ_BASE"/bin/setenv.sh
elif [ -r "$FFMQ_HOME"/bin/setenv.sh ]; then
  . "$FFMQ_HOME"/bin/setenv.sh
fi

# Config file path
CONFIG=$FFMQ_BASE/conf/ffmq-server.properties

# Auto-create classpath
CLASSPATH=""
for lib in $FFMQ_HOME/lib/*.jar;
do
  CLASSPATH="${CLASSPATH}:$lib"
done

# Run the queuer
nohup java $JAVA_OPTS -DFFMQ_HOME="$FFMQ_HOME" -DFFMQ_BASE="$FFMQ_BASE" -cp "$CLASSPATH" net.timewalker.ffmq4.FFMQServerLauncher -conf "$CONFIG" 1> stdout.log 2> stderr.log &
SERVER_PID=$!
echo $SERVER_PID > $FFMQ_BASE/bin/ffmq-server.pid

echo "Server started with pid $SERVER_PID, see logs in $FFMQ_BASE/logs/"

