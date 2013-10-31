#!/bin/sh

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Get application home
[ -z "$FFMQ_HOME" ] && FFMQ_HOME=`cd "$PRGDIR/.." ; pwd`

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

# Create classpath
CLASSPATH=$FFMQ_HOME/lib/commons-logging-1.1.jar:$FFMQ_HOME/lib/log4j-1.2.15.jar:$FFMQ_HOME/lib/jms-1.1.jar:$FFMQ_HOME/lib/mx4j-3.0.2.jar:$FFMQ_HOME/lib/mx4j-remote-3.0.2.jar
CLASSPATH=$CLASSPATH:$FFMQ_HOME/lib/ffmq3-core.jar:$FFMQ_HOME/lib/ffmq3-server.jar

# Run the queuer
cd $PRGDIR || exit 1
nohup java $JAVA_OPTS -DFFMQ_HOME="$FFMQ_HOME" -DFFMQ_BASE="$FFMQ_BASE" -cp "$CLASSPATH" net.timewalker.ffmq3.FFMQServerLauncher -conf "$CONFIG" 1> stdout.log 2> stderr.log &
SERVER_PID=$!
echo $SERVER_PID > $FFMQ_BASE/bin/ffmq-server.pid

echo "Server started with pid $SERVER_PID, see logs in $FFMQ_BASE/logs/"

