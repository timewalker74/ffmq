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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import net.timewalker.ffmq4.common.message.selector.expression.utils.StringUtils;
import net.timewalker.ffmq4.jmx.JMXAgent;
import net.timewalker.ffmq4.management.ManagementUtils;
import net.timewalker.ffmq4.utils.InetUtils;
import net.timewalker.ffmq4.utils.Settings;
import net.timewalker.ffmq4.utils.pool.ObjectPool;

/**
 * FFMQJMXConsole
 */
public final class FFMQJMXConsole implements Runnable
{
	// Attributes
	private Settings settings;
	
	// Runtime
	private JMXServiceURL jmxServiceURL; 
	private JMXConnector connector;
	private MBeanServerConnection connection;
	private boolean stopRequired;
	
	// Output
    private PrintStream out;
    private PrintStream err;
	private BufferedReader in;
	
	/**
	 * Constructor
	 */
	public FFMQJMXConsole( Settings settings , InputStream in , PrintStream out , PrintStream err )
	{
		this.settings = settings;
		this.in = new BufferedReader(new InputStreamReader(in));
		this.out = out;
		this.err = err;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try
		{
			String serverHost = InetUtils.resolveAutoInterfaceAddress(settings.getStringProperty(FFMQJMXConsoleSettings.SERVER_HOST, "localhost"));
			int serverPort = settings.getIntProperty(FFMQJMXConsoleSettings.SERVER_PORT, 10003);

			String serviceUrl = "service:jmx:rmi://"+serverHost+"/jndi/rmi://"+serverHost+":"+serverPort+"/jmxconnector-FFMQ-server";
			out.println("JMX Service URL : "+serviceUrl);
			this.jmxServiceURL = new JMXServiceURL(serviceUrl);

			printServerVersion();
			
			boolean interactive = settings.getBooleanProperty(FFMQJMXConsoleSettings.INTERACTIVE, false);
			if (interactive)
				interactiveMode();
			else
			{
				String command = settings.getStringProperty(FFMQJMXConsoleSettings.COMMAND, "help");
				processCommand(command);
			}
		}
		catch (Exception e)
		{
			closeJMXResources();
			handleException(e);
		}
	}
	
	private void interactiveMode() throws Exception
	{
		out.println("FFMQ JMX Console");
		out.println("----------------");
		
		while (!stopRequired)
		{
			out.print("> ");
			out.flush();
			
			String command = in.readLine();
			if (command == null)
			    break;
			command = command.trim();
			
			try
			{
				if (command.length() > 0)
					processCommand(command);
			}
			catch (Exception e)
			{
				closeJMXResources();
				handleException(e);
			}
		}
	}
	
	private String[] splitCommand( String command )
	{
		StringTokenizer st = new StringTokenizer(command," \t");
		String[] tokens = new String[st.countTokens()];
		int pos = 0;
		while (st.hasMoreTokens())
			tokens[pos++] = st.nextToken();
		return tokens;
	}
	
	private void processCommand( String command ) throws Exception
	{
		String[] commandTokens = splitCommand(command);
		
		switch (commandTokens.length)
		{
			case 1 :
				if (command.equals("help"))
					printHelp();
				else
				if (command.equals("serverstatus"))
					printServerStatus();
				else
				if (command.equals("enginesstatus"))
					printEnginesStatus();
				else
				if (command.equals("listenersstatus"))
					printListenersStatus();
				else
				if (command.equals("bridgesstatus"))
					printBridgesStatus();
				else
				if (command.equals("fullstatus"))
					printFullStatus();
				else
				if (command.equals("quit") || command.equals("exit"))
				{
					stopRequired = true;
					out.println("Exiting.");
				}
				else
					err.println("Invalid command : "+command);
			break;
			
			case 2 :
				if (commandTokens[0].equals("enginestatus"))
				{
					printEngineStatus(commandTokens[1]);
				}
				else
				if (commandTokens[0].equals("listenerstatus"))
				{
					printListenerStatus(commandTokens[1]);
				}
				else
				if (commandTokens[0].equals("bridgestatus"))
				{
					printBridgeStatus(commandTokens[1]);
				}
				else
					err.println("Invalid command : "+command);
			break;
			
			default:
				err.println("Invalid command : "+command);
		}
	}
	
	private void handleException( Exception e )
	{
		if (e instanceof InvocationTargetException)
		{
			Throwable cause = ((InvocationTargetException) e).getTargetException();
			if (cause instanceof Exception)
				handleException((Exception)cause);
			else
				throw (Error)cause;
		}
		else
		{
			err.println("ERROR: "+e.toString());
		}
	}
	
	private void printHelp()
	{
		out.println("Available commands :");
		out.println(" serverstatus    : display server status");
		out.println(" listenersstatus : display listeners status");
		out.println(" enginesstatus   : display engines status");
		out.println(" bridgesstatus   : display bridges status");
		out.println(" fullstatus      : display full server status");
		out.println(" enginestatus <engineName>     : display engine status");
		out.println(" listenerstatus <listenerName> : display listener status");		
		out.println(" bridgestatus <bridgeName>     : display bridge status");
		out.println(" help            : display console help");
		out.println(" exit/quit       : exit the console application");
	}
	
	private void printFullStatus() throws Exception
	{
		out.println("=============================");
		out.println("    FULL SERVER STATUS");
		out.println("=============================");
		
		printServerStatus();
		printListenersStatus();
		printEnginesStatus();
		printBridgesStatus();
	}
	
	private void printServerVersion() throws Exception
	{
		MBeanServerConnection conn = getMBeanServerConnection();
		
		ObjectName serverON = new ObjectName(JMXAgent.JMX_DOMAIN+":type=Server");
		String version = (String)conn.getAttribute(serverON, "Version");
		
		out.println("Connected to server version "+version);
	}

	private void printServerStatus() throws Exception
	{
		MBeanServerConnection conn = getMBeanServerConnection();
		
		ObjectName serverON = new ObjectName(JMXAgent.JMX_DOMAIN+":type=Server");
		Boolean started = (Boolean)conn.getAttribute(serverON,"Started");
		String version = (String)conn.getAttribute(serverON, "Version");
		Long uptime = (Long)conn.getAttribute(serverON, "Uptime");
		Boolean remoteAdmin = (Boolean)conn.getAttribute(serverON,"RemoteAdministrationEnabled");
		
		out.println("Server (started="+started+")");
		out.println("  Version        : "+version);
		out.println("  Uptime         : "+formatDelay(uptime.longValue()));
		out.println("  Remote admin.  : "+(remoteAdmin != null && remoteAdmin.booleanValue() ? "ENABLED":"DISABLED"));
	}
	
	private void printListenersStatus() throws Exception
	{
		MBeanServerConnection conn = getMBeanServerConnection();
		Iterator<ObjectInstance> listenerInstances = conn.queryMBeans(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Listeners,listener=*"), null).iterator();
		while (listenerInstances.hasNext())
		{
			ObjectInstance listenerInstance = listenerInstances.next();
			String listenerName = (String)conn.getAttribute(listenerInstance.getObjectName(),"Name");
			
			printListenerStatus(listenerName);
		}
	}
	
	private void printListenerStatus( String listenerName ) throws Exception
	{

		MBeanServerConnection conn = getMBeanServerConnection();
		ObjectName listenerON = new ObjectName(JMXAgent.JMX_DOMAIN+":type=Listeners,listener="+listenerName);

		try
		{
			conn.getObjectInstance(listenerON);
		}
		catch (InstanceNotFoundException e)
		{
			err.println("No such listener : "+listenerName);
			return;
		}
						
		Boolean started = (Boolean)conn.getAttribute(listenerON,"Started");
		Integer activeClients = (Integer)conn.getAttribute(listenerON, "ActiveClients");
		Integer acceptedTotal = (Integer)conn.getAttribute(listenerON, "AcceptedTotal");
		Integer droppedTotal = (Integer)conn.getAttribute(listenerON, "DroppedTotal");
		Integer maxActiveClients = (Integer)conn.getAttribute(listenerON, "MaxActiveClients");
		Integer capacity = (Integer)conn.getAttribute(listenerON, "Capacity");
			
		out.println("Listener ["+listenerName+"] (started="+started+")");
		out.println("  Active clients : "+activeClients+" (peak="+maxActiveClients+",max="+capacity+")");
		out.println("  Accepted total : "+acceptedTotal);
		out.println("  Dropped total  : "+droppedTotal);
	}
	
	private void printBridgesStatus() throws Exception
	{
		MBeanServerConnection conn = getMBeanServerConnection(); 
		Iterator<ObjectInstance> bridgeInstances = conn.queryMBeans(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Bridges,bridge=*"), null).iterator();
		while (bridgeInstances.hasNext())
		{
			ObjectInstance bridgeInstance = bridgeInstances.next();
			String bridgeName = (String)conn.getAttribute(bridgeInstance.getObjectName(),"Name");
			
			printBridgeStatus(bridgeName);
		}
	}
	
	private void printBridgeStatus( String bridgeName ) throws Exception
	{
		MBeanServerConnection conn = getMBeanServerConnection();
		ObjectName engineON = new ObjectName(JMXAgent.JMX_DOMAIN+":type=Bridges,bridge="+bridgeName);

		try
		{
			conn.getObjectInstance(engineON);
		}
		catch (InstanceNotFoundException e)
		{
			err.println("No such bridge : "+bridgeName);
			return;
		}

		Boolean started = (Boolean)conn.getAttribute(engineON,"Started");
		Long failures = (Long)conn.getAttribute(engineON,"Failures");
		Long forwardedMessages = (Long)conn.getAttribute(engineON,"ForwardedMessages");
		Boolean commitSourceFirst = (Boolean)conn.getAttribute(engineON,"CommitSourceFirst");
		Boolean consumerTransacted = (Boolean)conn.getAttribute(engineON,"ConsumerTransacted");
		Boolean producerTransacted = (Boolean)conn.getAttribute(engineON,"ProducerTransacted");
		Integer consumerAckMode = (Integer)conn.getAttribute(engineON,"ConsumerAcknowledgeMode");
		Integer producerDeliveryMode = (Integer)conn.getAttribute(engineON,"ProducerDeliveryMode");
		Integer retryInterval = (Integer)conn.getAttribute(engineON,"RetryInterval");
		
		out.println("Bridge ["+bridgeName+"]");
		out.println("  Started                : "+(started != null && started.booleanValue() ? "YES":"NO"));
		out.println("  Forwarded Messages     : "+forwardedMessages);
		out.println("  Failures               : "+failures);
		out.println("  Commit source first    : "+(commitSourceFirst != null && commitSourceFirst.booleanValue() ? "YES":"NO"));
		out.println("  Consumer transacted    : "+(consumerTransacted != null && consumerTransacted.booleanValue() ? "YES":"NO"));
		out.println("  Producer transacted    : "+(producerTransacted != null && producerTransacted.booleanValue() ? "YES":"NO"));
		out.println("  Consumer Ack Mode      : "+ManagementUtils.acknowledgeModeAsString(consumerAckMode.intValue()));
		out.println("  Producer Delivery Mode : "+ManagementUtils.deliveryModeAsString(producerDeliveryMode.intValue()));
		out.println("  Retry interval         : "+retryInterval+"s");
	}
	
	private void printEnginesStatus() throws Exception
	{
		MBeanServerConnection conn = getMBeanServerConnection(); 
		Iterator<ObjectInstance> engineInstances = conn.queryMBeans(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine=*"), null).iterator();
		while (engineInstances.hasNext())
		{
			ObjectInstance engineInstance = engineInstances.next();
			String engineName = (String)conn.getAttribute(engineInstance.getObjectName(),"Name");
			
			printEngineStatus(engineName);
		}
	}
	
	private void printEngineStatus( String engineName ) throws Exception
	{
		MBeanServerConnection conn = getMBeanServerConnection();
		ObjectName engineON = new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engineName);

		try
		{
			conn.getObjectInstance(engineON);
		}
		catch (InstanceNotFoundException e)
		{
			err.println("No such engine : "+engineName);
			return;
		}
		
		Boolean deployed = (Boolean)conn.getAttribute(engineON,"Deployed");
		
		out.println("Engine ["+engineName+"] (deployed="+deployed+")");
		out.println("  Queues");
		printEngineQueues(engineName,"    ");
		out.println("  Topics");
		printEngineTopics(engineName,"    ");
		out.println("  Async. Managers");
		printEngineAsyncManagers(engineName,"    ");
	}
	
	private void printEngineQueues(String engineName,String indent) throws Exception
	{
		MBeanServerConnection conn = getMBeanServerConnection(); 
		Iterator<ObjectInstance> queueInstances = conn.queryMBeans(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engineName+",children=queues,name=*"), null).iterator();
		while (queueInstances.hasNext())
		{
			ObjectInstance queueInstance = queueInstances.next();
			String queueName = (String)conn.getAttribute(queueInstance.getObjectName(),"Name");
			
			Long sentToQueueCount = (Long)connection.getAttribute(queueInstance.getObjectName(), "SentToQueueCount");
			Long receivedFromQueueCount = (Long)connection.getAttribute(queueInstance.getObjectName(), "ReceivedFromQueueCount");
			Long acknowledgedGetCount = (Long)connection.getAttribute(queueInstance.getObjectName(), "AcknowledgedGetCount");
			Long rollbackedGetCount = (Long)connection.getAttribute(queueInstance.getObjectName(), "RollbackedGetCount");
			Long expiredCount = (Long)connection.getAttribute(queueInstance.getObjectName(), "ExpiredCount");
			Integer size = (Integer)connection.getAttribute(queueInstance.getObjectName(), "Size");
			Integer registeredConsumersCount = (Integer)connection.getAttribute(queueInstance.getObjectName(), "RegisteredConsumersCount");
			
			out.print(indent+StringUtils.rightPad(queueName,32,' '));
			out.print(" - size="+size+" consumers="+registeredConsumersCount+" ack="+acknowledgedGetCount+" rollback="+rollbackedGetCount+" expired="+expiredCount);
			out.println(" sentTo="+sentToQueueCount+" receivedFrom="+receivedFromQueueCount);
		}
	}

	private void printEngineTopics(String engineName,String indent) throws Exception
	{
		MBeanServerConnection conn = getMBeanServerConnection(); 
		Iterator<ObjectInstance> topicInstances = conn.queryMBeans(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engineName+",children=topics,name=*"), null).iterator();
		while (topicInstances.hasNext())
		{
			ObjectInstance topicInstance = topicInstances.next();
			String topicName = (String)conn.getAttribute(topicInstance.getObjectName(),"Name");
			
			Long sentToTopicCount = (Long)connection.getAttribute(topicInstance.getObjectName(), "SentToTopicCount");
			Long dispatchedFromTopicCount = (Long)connection.getAttribute(topicInstance.getObjectName(), "DispatchedFromTopicCount");
			Integer size = (Integer)connection.getAttribute(topicInstance.getObjectName(), "Size");
			Integer registeredConsumersCount = (Integer)connection.getAttribute(topicInstance.getObjectName(), "RegisteredConsumersCount");
			
			out.print(indent+StringUtils.rightPad(topicName,32,' '));
			out.print(" - size="+size+" consumers="+registeredConsumersCount);
			out.println(" sentTo="+sentToTopicCount+" dispatchedFrom="+dispatchedFromTopicCount);
		}
	}
	
	private void printEngineAsyncManagers(String engineName,String indent) throws Exception
	{
		MBeanServerConnection conn = getMBeanServerConnection(); 
		Iterator<ObjectInstance> asyncManagerInstances = conn.queryMBeans(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engineName+",children=async-managers,name=*"), null).iterator();
		while (asyncManagerInstances.hasNext())
		{
			ObjectInstance asyncManagerInstance = asyncManagerInstances.next();
			String asyncManagerName = (String)conn.getAttribute(asyncManagerInstance.getObjectName(),"Name");
			
			Integer taskQueueSize = (Integer)connection.getAttribute(asyncManagerInstance.getObjectName(), "TaskQueueSize");
			
			Integer threadPoolAvailableCount = (Integer)connection.getAttribute(asyncManagerInstance.getObjectName(), "ThreadPoolAvailableCount");
			Integer threadPoolExhaustionPolicy = (Integer)connection.getAttribute(asyncManagerInstance.getObjectName(), "ThreadPoolExhaustionPolicy");
			Integer threadPoolMaxIdle = (Integer)connection.getAttribute(asyncManagerInstance.getObjectName(), "ThreadPoolMaxIdle");
			Integer threadPoolMaxSize = (Integer)connection.getAttribute(asyncManagerInstance.getObjectName(), "ThreadPoolMaxSize");
			Integer threadPoolMinSize = (Integer)connection.getAttribute(asyncManagerInstance.getObjectName(), "ThreadPoolMinSize");
			Integer threadPoolPendingWaits = (Integer)connection.getAttribute(asyncManagerInstance.getObjectName(), "ThreadPoolPendingWaits");
			Integer threadPoolSize = (Integer)connection.getAttribute(asyncManagerInstance.getObjectName(), "ThreadPoolSize");
			Long threadPoolWaitTimeout = (Long)connection.getAttribute(asyncManagerInstance.getObjectName(), "ThreadPoolWaitTimeout");

			out.println(indent+"["+asyncManagerName+"]");
			out.println(indent+"  Task queue size                 : "+taskQueueSize);
			out.println(indent+"  Thread Pool - Size              : "+threadPoolSize);
			out.println(indent+"  Thread Pool - Available         : "+threadPoolAvailableCount);
			out.println(indent+"  Thread Pool - Pending           : "+threadPoolPendingWaits);
			out.println(indent+"  Thread Pool - Min. Size         : "+threadPoolMinSize);
			out.println(indent+"  Thread Pool - Max. Size         : "+threadPoolMaxSize);
			out.println(indent+"  Thread Pool - Max. Idle         : "+threadPoolMaxIdle);
			out.println(indent+"  Thread Pool - Max. Idle         : "+threadPoolMaxIdle);
			out.println(indent+"  Thread Pool - Exhaustion Policy : "+ObjectPool.exhaustionPolicyAsString(threadPoolExhaustionPolicy.intValue()));
			out.println(indent+"  Thread Pool - Wait timeout      : "+threadPoolWaitTimeout+"ms");
		}
	}
	
	private JMXConnector getConnector() throws Exception
	{
		if (connector == null)
			connector = JMXConnectorFactory.connect(jmxServiceURL);
		return connector;
	}

	private MBeanServerConnection getMBeanServerConnection() throws Exception
	{
		if (connection == null)
			connection = getConnector().getMBeanServerConnection();
		return connection;
	}
	
	private void closeJMXResources()
	{
		connection = null;
		if (connector != null)
		{
			try
			{
				connector.close();
			}
			catch (Exception e)
			{
				err.println("Cannot close JMX connector");
				e.printStackTrace(err);
			}
			finally
			{
				connector = null;
			}
		}
	}

	private String formatDelay( long delay )
	{
		StringBuilder sb = new StringBuilder();
		
		if (delay < 0)
		{
			delay = -delay;
			sb.append("-");
		}
		
		long days = delay / (1000*60*60*24);
		if (days > 0)
		{
			sb.append(days);
			sb.append("d");
			delay = delay % (1000*60*60*24);
		}
		
		long hours = delay / (1000*60*60);
		if (hours > 0)
		{
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(hours);
			sb.append("h");
			delay = delay % (1000*60*60);
		}
		
		long minutes = delay / (1000*60);
		if (minutes > 0)
		{
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(minutes);
			sb.append("min");
			delay = delay % (1000*60);
		}
		
		long seconds = delay / 1000;
		if (seconds > 0)
		{
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(seconds);
			sb.append("s");
			delay = delay % 1000;
		}
		
		if (sb.length() > 0)
			sb.append(" ");
		sb.append(delay);
		sb.append("ms");
		
		return sb.toString();
	}
}
