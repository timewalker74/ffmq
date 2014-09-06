### What is FFMQ ?

FFMQ is a full-java, light-weight, fast JMS 1.1 queuer implementation.

Emphasis is made on raw performance and reliability.
As a consequence, it does not provide a lot of features but it's small, fast, easy to configure and has reduced dependencies.

### License

FFMQ is released under the GNU LGPL v3 license in order to be used both as a standalone application and/or integrated in all kind of software.

### Features

* JMS 1.1 compliant (almost, see limits below)
*    Lightweight (full server is below 600KB)
*    Reduced dependencies : 
   * (3.x branch) JRE 1.4+, JMS API, commons-logging, [optional: log4j, mx4j]
   * (4.x branch) JRE 1.5+, JMS API, commons-logging, [optional: log4j]
*    JMX monitoring support
*    SSL support for remote connections
*    Fast TCP-based network protocol
*    State-of-the-art asynchronous journaling persistence storage
*    Template-based destination definitions for easy configuration
*    JMS bridging support to pipe messages between destinations and/or queuers

### Limits

The following required JMS operations are not yet implemented :

    JMSXGroupID / JMSXGroupSeq message properties

The following optional JMS operations are not yet implemented :

    ConnectionConsumers and DurableConnectionConsumers
    Session MessageListener
    XAConnections

Other limitations :

    Durable subscriptions are lost on server restart
    Because FFMQ is using a separate disk store for each queue, atomicity of transactions spanning multiple destinations cannot be fully guaranteed in case of server failure. Per-queue atomicity is guaranteed.

### Performance

FFMQ is fast, real fast.

Anyway, you should never trust any written performance claim.
The best way to have an exact idea is to test it by yourself ! Please give it a try, if you have a JMS compliant application or benchmark this should be pretty easy (See 'Quick Start' below).

### Architecture

To get a technical insight on FFMQ inner workings you may want to have a look at the Technical Overview page.

### Quick Start

Just unzip the server package somewhere and start the server using the ffmq-server.bat or ffmq-server.sh shell in the bin/ directory.
(If necessary you can change default listen ports and interfaces in the conf/ffmq-server.properties file)

On the client-side, you need the ffmq-core.jar in your classpath. (plus commons-logging and log4j if you don't already have them)
Here is the default JNDI configuration to use :

    Naming Context Factory : net.timewalker.ffmq3.jndi.FFMQInitialContextFactory
    Connection Factory JNDI Name : factory/ConnectionFactory
    Provider URL : tcp://<hostname>:10002

### Bug reports / Contact

If you find any bug or problem, you can send me an email to : ffmq@timewalker.net

If you use FFMQ, successfully or not, I would be glad to hear from you. Tell me what you like or dislike about this piece of software.
