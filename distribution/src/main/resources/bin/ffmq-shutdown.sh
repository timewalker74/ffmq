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

PID_FILE=$FFMQ_BASE/bin/ffmq-server.pid

if [ -r "$PID_FILE" ]; then
    echo "Stopping server using PID file ..."
    PID=`cat "$PID_FILE"`
    if `ps -p $PID > /dev/null`; then
      kill $PID || exit 1
    
      # Wait loop (up to 30s)
      i=1;
      while [ $i -lt 30 ]; do
          sleep 1
          if ! $(ps -p $PID > /dev/null); then
              # Remove pidfile
              rm "$PID_FILE" || exit 1
              echo "Server stopped."  
              exit 0
          fi
          i=$(( $i + 1 ))
      done

      # Let's get brutal
      echo "Process $PID did not terminate normally, killing ..."
      kill -9 $PID || exit 1

      # Remove pidfile
      rm "$PID_FILE" || exit 1
    fi
else
    echo "PID file not found : $PID_FILE"
fi
