@echo off

set CLASSPATH=../lib/commons-logging-1.1.jar;../lib/log4j-1.2.15.jar;../lib/jms-1.1.jar
set CLASSPATH=%CLASSPATH%;../lib/ffmq4-core.jar;../lib/ffmq4-server.jar

java -Xmx64m -cp "%CLASSPATH%" net.timewalker.ffmq4.FFMQJMXConsoleLauncher %*