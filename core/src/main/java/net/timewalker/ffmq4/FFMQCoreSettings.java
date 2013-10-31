/*
 * This file is part of FFMQ.
 *
 * FFMQ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * FFMQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FFMQ; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.timewalker.ffmq4;

/**
 * FFMQCoreSettings
 */
public final class FFMQCoreSettings
{    
    // Management
    public static final String DESTINATION_DEFINITIONS_DIR = "management.destinationDefinitions.directory";
    public static final String BRIDGE_DEFINITIONS_DIR      = "management.bridgeDefinitions.directory";
    public static final String AUTO_CREATE_QUEUES          = "management.autoCreate.queues";
    public static final String AUTO_CREATE_TOPICS          = "management.autoCreate.topics";
    public static final String TEMPLATES_DIR               = "management.templates.directory";
    public static final String TEMPLATE_MAPPING_FILE       = "management.templates.mapping";
    public static final String DEFAULT_DATA_DIR            = "management.defaultData.directory";
    public static final String JMX_AGENT_ENABLED           = "management.jmx.agent.enabled";
    public static final String JMX_AGENT_JNDI_RMI_PORT     = "management.jmx.agent.jndi.rmi.port";
    public static final String JMX_AGENT_RMI_LISTEN_ADDR   = "management.jmx.agent.rmi.listenAddr";
    public static final String DEPLOY_QUEUES_ON_STARTUP    = "management.deployOnStartup.queues";
    public static final String DEPLOY_TOPICS_ON_STARTUP    = "management.deployOnStartup.topics";
    
    // Security related
    public static final String SECURITY_ENABLED    = "security.enabled";
    public static final String SECURITY_CONNECTOR  = "security.connector";

    // Prefetching
    public static final String CONSUMER_PREFETCH_SIZE      = "consumer.prefetch.size";
    
	// Asynchronous task managers
    public static final String ASYNC_TASK_MANAGER_NOTIFICATION_THREAD_POOL_MINSIZE = "asyncTaskManager.notification.threadPool.minSize";
	public static final String ASYNC_TASK_MANAGER_NOTIFICATION_THREAD_POOL_MAXIDLE = "asyncTaskManager.notification.threadPool.maxIdle";
	public static final String ASYNC_TASK_MANAGER_NOTIFICATION_THREAD_POOL_MAXSIZE = "asyncTaskManager.notification.threadPool.maxSize";
	public static final String ASYNC_TASK_MANAGER_DELIVERY_THREAD_POOL_MINSIZE = "asyncTaskManager.delivery.threadPool.minSize";
	public static final String ASYNC_TASK_MANAGER_DELIVERY_THREAD_POOL_MAXIDLE = "asyncTaskManager.delivery.threadPool.maxIdle";
	public static final String ASYNC_TASK_MANAGER_DELIVERY_THREAD_POOL_MAXSIZE = "asyncTaskManager.delivery.threadPool.maxSize";
	public static final String ASYNC_TASK_MANAGER_DISKIO_THREAD_POOL_MINSIZE   = "asyncTaskManager.diskIO.threadPool.minSize";
	public static final String ASYNC_TASK_MANAGER_DISKIO_THREAD_POOL_MAXIDLE   = "asyncTaskManager.diskIO.threadPool.maxIdle";
	public static final String ASYNC_TASK_MANAGER_DISKIO_THREAD_POOL_MAXSIZE   = "asyncTaskManager.diskIO.threadPool.maxSize";
	
	// Watchdog
	public static final String WATCHDOG_CONSUMER_INACTIVITY_TIMEOUT     = "watchdog.consumer.inactivityTimeout";

	// Notification
	public static final String NOTIFICATION_QUEUE_MAX_SIZE              = "notification.queue.maxSize";
	
	// Delivery
	public static final String DELIVERY_REDELIVERY_DELAY                = "delivery.redeliveryDelay";
	public static final String DELIVERY_LOG_LISTENERS_FAILURES          = "delivery.logListenersFailures";
	
	// TCP Transport
	public static final String TRANSPORT_TCP_PING_INTERVAL              = "transport.tcp.pingInterval";
	public static final String TRANSPORT_TCP_SEND_QUEUE_MAX_SIZE        = "transport.tcp.sendQueueMaxSize";
	public static final String TRANSPORT_TCP_STREAM_SEND_BUFFER_SIZE    = "transport.tcp.stream.sendBufferSize";
	public static final String TRANSPORT_TCP_STREAM_RECV_BUFFER_SIZE    = "transport.tcp.stream.recvBufferSize";
	public static final String TRANSPORT_TCP_INITIAL_PACKET_BUFFER_SIZE = "transport.tcp.initialPacketBufferSize";
	public static final String TRANSPORT_TCP_SOCKET_SEND_BUFFER_SIZE    = "transport.tcp.socket.sendBufferSize";
	public static final String TRANSPORT_TCP_SOCKET_RECV_BUFFER_SIZE    = "transport.tcp.socket.recvBufferSize";
	public static final String TRANSPORT_TCP_PACKET_MAX_SIZE            = "transport.tcp.packet.maxSize";
	// SSL
	public static final String TRANSPORT_TCP_SSL_ENABLED                = "transport.tcp.ssl.enabled";
    public static final String TRANSPORT_TCP_SSL_PROTOCOL               = "transport.tcp.ssl.protocol";
    public static final String TRANSPORT_TCP_SSL_KEYMANAGER_ALGORITHM   = "transport.tcp.ssl.keyManager.algorithm";
    public static final String TRANSPORT_TCP_SSL_KEYSTORE_TYPE          = "transport.tcp.ssl.keyStore.type";
    public static final String TRANSPORT_TCP_SSL_KEYSTORE_PATH          = "transport.tcp.ssl.keyStore.path";
    public static final String TRANSPORT_TCP_SSL_KEYSTORE_PASWORD       = "transport.tcp.ssl.keyStore.password";
    public static final String TRANSPORT_TCP_SSL_KEYSTORE_KEY_PASSWORD  = "transport.tcp.ssl.keyStore.keyPassword";
}
