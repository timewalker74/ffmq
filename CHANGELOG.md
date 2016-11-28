### FFMQ ChangeLog

## 4.0.0 (not yet released)

- **[Feature]** Upgrade project descriptors to Maven 3
- **[Feature]** Upgraded source/binary level to java 7
- **[Feature]** Use JDK concurrent structures instead of custom ones 
- **[Feature]** Use JDK JMX support and removed mx4j lib usage
- **[Fix]** Activity watchdog was not removing its entries properly
- **[Fix]** Ping packets were not being sent on TCP connections during long transactions, causing premature timeouts
- **[Fix]** Make sure ExceptionListeners are not called by mission-critical threads that cannot afford to wait
- **[Feature]** Add more consistency checks during templates and destinations definitions load
- **[Fix]** LocalQueueMBean getPersistentStoreUsage() should use absolute storage usage instead of current store usage if queue can auto-extend

 `FFMQ 4.0.x branch was forked from FFMQ 3.0.5`

## 3.0.8

- **[Fix]** LocalQueueMBean getPersistentStoreUsage() should use absolute storage usage instead of current store usage if queue can auto-extend

## 3.0.7

- **[Feature]** Some code and javadoc cleanups
- **[Feature]** Enabled standard sources and javadoc generation
- **[Feature]** Improved POM descriptors + support to upload to sonatype repository

## 3.0.6

- **[Fix]** Activity watchdog was not removing its entries properly
- **[Fix]** Ping packets were not being sent on TCP connections during long transactions, causing premature timeouts
- **[Fix]** Make sure ExceptionListeners are not called by mission-critical threads that cannot afford to wait
- **[Feature]** Add more consistency checks during templates and destinations definitions load

## 3.0.5

- **[Fix]** Fix getStoreUsage() returning an invalid percentage for persistent stores


## 3.0.4

- **[Fix]** Fix listener JMX domain


## 3.0.3

- **[Feature]** JMX : also serve plateform MBeans when available
- **[Fix]** Java 1.4 runtime compatibility was broken in several places !


## 3.0.2

- **[Fix]** Make sure UUIDProvider does not crash on startup if the localhost address is not available for whatever reason (no available network interface or misconfigured hostname).


## 3.0.1

- **[Fix]** Transport endpoint : make sure we do not recycle an old response if the transport is concurrently closed. This fixes a race condition were a commit could return successfully although no acknowledge was received.


## 3.0.0

- **[Feature]** New remote admin command : purgeQueue
- **[Feature]** New configurable topic subscriber policy feature in case of subscriber failure or queue full. (See 'subscriberFailurePolicy' and 'subscriberOverflowPolicy' topic properties)
- **[Feature]** Support for network interface address auto-discovery using special notation 'auto:eth0' for example
- **[Feature]** More JMX support : expired message count, store usage
- **[Fix]** Fixed SentToQueueCount counter behavior
- **[Fix]** Various cleanups and tunings


## 3.0.0-rc8

- **[Feature]** Disabled automatic overflow to persistent store by default (not a JMS compliant feature : breaks message ordering). Added new setting to re-enable it.


## 3.0.0-rc7

- **[Feature]** Added some javabean properties to connection factories to improve usability in third-party JMS tools.
- **[Fix]** Fixed BytesMessage reset() only working the first time.


## 3.0.0-rc6

- **[Feature]** Feature to auto-retry message put if remote queue is full
- **[Fix]** Do not clear pending messages if commit fails
- **[Fix]** Fixed invalid read/write lock usage


## 3.0.0-rc5

- **[Feature]** Tuned message serialization (changed protocol version)
- **[Feature]** Journal files recycling
- **[Feature]** Auto prune unnecessary journal operations to improve performance
- **[Feature]** Even more JMX
- **[Fix]** Synchronized message stores random access files more to avoid subtle concurrency bugs
- **[Fix]** Reworked destination locking during commit/rollback operations


## 3.0.0-rc4

- **[Feature]** More JMX support
- **[Feature]** Turned off journal files pre-allocation by default (this was a bit agressive for common use-cases)
- **[Feature]** More documentation
- **[Fix]** Fixed ping responses being filtered out, causing client timeouts when idle
- **[Fix]** Made endpoints more re-active on connection close
- **[Fix]** Various cleanups


## 3.0.0-rc3

- **[Feature]** Improved journaling store write concurrency
- **[Feature]** Lazily create journal files on first write
- **[Feature]** Added setting to choose the disk sync method (defaults to channel.force(false))
- **[Feature]** Journal files are now pre-allocated by default to improve performance
- **[Fix]** Messages received through local listeners were not properly de-serialized
- **[Fix]** Removing a message from store could corrupt the priority table
- **[Fix]** Shutdown on SIGINT was not properly waiting for the engine to stop, causing full recovery on next startup


## 3.0.0-rc2

- **[Feature]** Reworked the internal notification pipeline to improve throughput
- **[Feature]** Do not reply to async network requests to save bandwidth
- **[Feature]** Improved remote client prefetching
- **[Feature]** Lot of cleanups
- **[Feature]** New synchronization architecture to handle concurrent closing of resources
- **[Fix]** Reworked transaction boundaries : make sure everything is synced before waking up consumers
- **[Fix]** Correctly re-acquire durable subscriptions (preventing a subscription leak).


## 3.0.0-rc1

- **[Feature]** Lot of performance tunings
- **[Feature]** Rewrote the persistence layer (journal-based, asynchronous)
- **[Feature]** Asynchronous acknowledgment of delivered messages
- **[Feature]** Improved architecture, easier to embed


## 2.1.4

- **[Fix]** Fixed BytesMessage reset() only working the first time.
- **[Fix]** Fixed a bug in datastore delete() that could lead to index corruption for certain message and block sizes.


## 2.1.3

- **[Fix]** Correctly re-acquire durable subscriptions (preventing a subscription leak).
- **[Fix]** Changed default max message size (was too low for a default value).


## 2.1.2

- **[Fix]** Fixed CopyOnWriteList synchronization (could crash under heavy consumer re-connect load).
- **[Fix]** Fixed a race condition in the session updated queues list that would cause some messages not to be properly committed in some situations.
- **[Fix]** Fixed some cases where an exception could abort an object close() too early
- **[Fix]** In-memory datastore had a different behavior from the persistent one when full.
- **[Fix]** If a volatile message was persisted because the in-memory datastore is full, the persistent store would not be properly synced.
- **[Fix]** Fixed a race-condition in the consumer round-robin dispatch algorithm that would cause starvation in some situations.
- **[Fix]** Fixed fairness of the consumer round-robin dispatch algorithm under heavy load.
- **[Fix]** Do not try to forward messages to consumers on a stopped connection.


## 2.1.1

- **[Fix]** Reworked locking in various places to fix concurrency issues
- **[Fix]** Workaround an issue with NIO and socket options on some platforms
- **[Feature]** Improved queue creation speed for volatile and/or temporary queues.


## 2.1.0

- **[Fix]** API cleanups, more javadoc.
- **[Fix]** Fixed various possible deadlocks when closing resources concurrently.
- **[Fix]** Increased default asynchronous queue max size.


## 2.0.12

- **[Feature]** Support for a redelivery delay after rollback. (see deliver.redeliverDelay setting)
- **[Fix]** Some JMS specification compliance fixes.


## 2.0.11

- **[Feature]** Drop clients that do not create a first session quickly enough. (Improves server robustness regarding buggy clients)

## 2.0.10

- **[Feature]** Display listener's clients in JMX.
- **[Fix]** Reverted 2.0.9 change. Did not work with generic JMX consoles.
- **[Fix]** Reworked transaction demarcation : prefetching did not work well with multiple session consumers.
- **[Fix]** Improved deferred file deletion to workaround JVM limitations with memory mapped files.


## 2.0.9

- **[Fix]** Fixed an issue with JMX over RMI on localhost that prevented clients to connect.


## 2.0.8

- **[Fix]** Fixed rollback issue when persistent storage is disabled.


## 2.0.7

- **[Security]** Made the server more robust regarding rogue connections stalled before authentication.
- **[Feature]** Support for tcp listener limit : do no accept more connections than specified by the listener capacity.
- **[Fix]** Made client properly close its connection when authentication fails.
- **[Fix]** Fixed spurious exception when a connection is auto-closed by garbage collection.


## 2.0.6

- **[Fix]** Context.SECURITY_PRINCIPAL and Context.SECURITY_CREDENTIALS were not taken into account when set in JDNI context.
- **[Fix]** Do not enforce initial packet size restriction on the client side. This was swallowing error messages returned by the server.


## 2.0.5

- **[Fix]** Fixed StreamMessage still read-only after calling clearBody(). Allow null objects to be stored in StreamMessage. Fixed ObjectMessage not being read-only upon reception.
- **[Fix]** Fixed int to long conversion in queue size/offset computation that would overflow for very large queues.
- **[Fix]** Fixed exception handling in NIO Tcp multiplexer initialization.
- **[Fix]** Made async processor threads daemon so they don't block server or client JVM exit 
- **[Feature]** Use shorter UUIDs for producer/consumers to save some bandwidth.
- **[Feature]** Fallback to persistent store if possible when a volatile store is full.


## 2.0.4

- **[Security]** Made the server more robust regarding invalid protocol packets


## 2.0.3

- **[Fix]** Fixed possible deadlock in activity watchdog


## 2.0.2

- **[Fix]** ByteMessage implementation was broken
- **[Fix]** Made the server shutdown script more portable (Thanks Nick)
- **[Fix]** Rare deadlock when closing a connection while the watchdog is active
- **[Fix]** Fixed possible deadlock when stopping a JMS Bridge
- **[Fix]** Auto-close connection on transport failure

## 2.0.1

- **[Fix]** Fixed crash when remotely deleting a temporary queue or topic
- **[Fix]** Fix issue with temporary topic name being too long



## 2.0.0

- **[Feature]** New message-push architecture for remote consumers with even-better latency
- **[Feature]** Reworked the asynchronous dispatch system for better scalability with increasing number of consumers
- **[Feature]** Use NIO memory map features to improve performance
- **[Feature]** New optional NIO based packet transport option with improved scalability. Available both on the server and client-side.
- **[Feature]** JMS Bridging support
- **[Feature]** Messages prefetching features for remote listeners to improve throughput
- **[Feature]** Reworked the JMX model.
- **[Feature]** Ping timeout support on remote connections to detect stalled consumers
- **[Fix]** Fixed a race condition in remote connection start
- **[Fix]** Do not dispatch messages if connection is not started
- **[Fix]** Fixed a socket leak on client disconnect
- **[Fix]** Issues with complex message selectors
- **[Fix]** Support for JMSRedelivered flag
- **[Fix]** Various small cleanups and specification compliance fixes


## 1.2.5

- **[Fix]** Fixed various bugs with messages priority that caused persisted messages priority to get messed up when the queuer was restarted, and other invalid ordering behaviors
- **[Fix]** Report network errors to exception listeners so they have a chance to reconnect
- **[Fix]** Don't allow multiple templates with the same name
- **[Fix]** Trim values in properties files to avoid issues with white space in config. values
- **[Fix]** Better server behavior regarding stalled remote consumers
- **[Feature]** Reworked all thrown JMS exceptions : they now contain an error code
- **[Feature]** Added a shutdown hook to allow for graceful server shutdown on SIGINT/SIGTERM


## 1.2.4

- **[Fix]** Fixed a bug causing the queue store to be corrupted when the queue is almost full and you put a message that won't fit


## 1.2.3

- **[Fix]** Take FFMQ_BASE system property into account when looking for a default config file


## 1.2.2

- **[Feature]** Added support for FFMQ_HOME and FFMQ_BASE system properties to spawn multiple server instances using the same installation
- **[Feature]** Added support for system property replacement in settings and descriptor files

## 1.2.1

- **[Fix]** (JMX) Enforce use of 'management.jmx.agent.rmi.listenAddr' for exported MBean servers too (was only used for the RMI registry)
- **[Fix]** (JMX) Added a workaround to close the MBeans RMI sockets when shutting down the JMX agent
- **[Fix]** Do not synchronize on client-provided message listeners to avoid possible deadlocks with external code.
- **[Fix]** Fixed localTopicExists() returning invalid values (thanks to <i>snappyh</i>)
- **[Fix]** Purge administrative queues before starting the remote administration agent
- **[Fix]** Fix the admin client so it doesn't wait for a server reply when issuing a shutdown command
- **[Fix]** The purge() method on a LocalQueue no longer removes locked message to avoid concurrency issues
- **[Fix]** mx4j is now an optional runtime dependency


## 1.2.0

- **[Feature]** Support for asynchronous delivery of notifications to MessageListeners using a thread pool. Slow message listeners no longer hang the whole server.
- **[Fix]** Detect unsupported delivery mode earlier when sending on a topic.
- **[Fix]** Destroying a temporary queue did not clear the associated pending changes in the current transaction, thus causing store exceptions on the next commit/rollback operation.
- **[Fix]** Temporary queues cleanup failed if lock files were present. Lock files are also deleted now.
- **[Fix]** Catch exceptions produced by invalid topic subscribers so they do not break the put operation.
- **[Fix]** JMS expiration was not taken into account. Expired messages are now lazily removed when reading or browsing a queue.
- **[Fix]** Redelivered flag is now correctly set on rollbacked messages. (It still does not survive a server shutdown, though)

## 1.1.0

- **[Feature]** Destinations can now be looked up in JNDI under the names : "queue/&lt;queueName&gt;" for queues and "topic/&lt;topicName&gt;" for topics.
- **[Feature]** Queue browsers are now implemented.
- **[Feature]** A new class allows for an easy integration in the spring framework : net.timewalker.ffmq.spring.FFMQServerBean
- **[Fix]** Pending get operations were incorrectly associated to their consumers instead of the parent session : closing a consumer would then rollback all pending get operations for that consumer. The consumer can now be closed independently of the session lifecycle.
- **[Fix]** Closing a remote consumer/producer would not close the associated objects on the server-side, causing a temporary leak until the session was finally closed.


## 1.0.0

- First release



