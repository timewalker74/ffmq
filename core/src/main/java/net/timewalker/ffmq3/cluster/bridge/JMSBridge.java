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
package net.timewalker.ffmq3.cluster.bridge;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.NamingException;

import net.timewalker.ffmq3.cluster.resolver.DestinationResolver;
import net.timewalker.ffmq3.cluster.resolver.SessionDestinationResolver;
import net.timewalker.ffmq3.management.bridge.BridgeDefinition;
import net.timewalker.ffmq3.management.peer.PeerDescriptor;
import net.timewalker.ffmq3.utils.JNDITools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Implements a JMS 'bridge', that is a message pipe between two destinations
 * (local or remote).
 * Each bridge uses a private, fail-safe and auto-retrying handler thread to copy 
 * messages from a source destination to a target destination.
 * </p>
 * <p>
 * A JMS Bridge behavior is defined using a BridgeDefinition descriptor.
 * </p>
 * @see BridgeDefinition
 */
public final class JMSBridge implements JMSBridgeMBean
{
	protected static final Log log = LogFactory.getLog(JMSBridge.class);
	
	// Attributes
	protected BridgeDefinition bridgeDefinition;
	
	// Runtime
	private JMSBridgeThread bridgeThread;
	protected volatile long forwardedMessages;
	protected volatile long failures;
	private boolean started;
	
	/**
	 * Constructor
	 */
	public JMSBridge( BridgeDefinition bridgeDefinition )
	{
		this.bridgeDefinition = bridgeDefinition;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeMBean#getName()
	 */
	public String getName()
	{
		return bridgeDefinition.getName();
	}
	
	/**
	 * @return the bridgeDefinition
	 */
	public BridgeDefinition getBridgeDefinition()
	{
		return bridgeDefinition;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeMBean#getForwardedMessages()
	 */
	public long getForwardedMessages()
	{
		return forwardedMessages;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeMBean#getFailures()
	 */
	public long getFailures()
	{
		return failures;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeMBean#resetStats()
	 */
	public void resetStats()
	{
		forwardedMessages = 0;
		failures = 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeMBean#start()
	 */
	public synchronized void start()
	{
		if (started)
			return;
		
		bridgeThread = new JMSBridgeThread();
		bridgeThread.start();
		log.info("["+bridgeDefinition.getName()+"] JMS bridge started");
		
		started = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeMBean#stop()
	 */
	public synchronized void stop()
	{
		if (!started)
			return;
		
		bridgeThread.pleaseStop();
		try
		{
			bridgeThread.join();
		}
		catch (InterruptedException e)
		{
			log.error("Wait for bridge thread completion was interrupted");
		}
		finally
		{
			bridgeThread = null;
		}
		
		log.info("["+bridgeDefinition.getName()+"] JMS bridge stopped.");
		started = false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeMBean#isStarted()
	 */
	public synchronized boolean isStarted()
	{
		return started;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeDefinitionMBean#getRetryInterval()
	 */
	public int getRetryInterval()
	{
		return bridgeDefinition.getRetryInterval();
	}

	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeDefinitionMBean#isCommitSourceFirst()
	 */
	public boolean isCommitSourceFirst()
	{
		return bridgeDefinition.isCommitSourceFirst();
	}

	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeDefinitionMBean#isProducerTransacted()
	 */
	public boolean isProducerTransacted()
	{
		return bridgeDefinition.isProducerTransacted();
	}

	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeDefinitionMBean#isConsumerTransacted()
	 */
	public boolean isConsumerTransacted()
	{
		return bridgeDefinition.isConsumerTransacted();
	}

	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeDefinitionMBean#getConsumerAcknowledgeMode()
	 */
	public int getConsumerAcknowledgeMode()
	{
		return bridgeDefinition.getConsumerAcknowledgeMode();
	}

	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeDefinitionMBean#getProducerDeliveryMode()
	 */
	public int getProducerDeliveryMode()
	{
		return bridgeDefinition.getProducerDeliveryMode();
	}

	//------------------------------------------------------------------------------
	
	private class JMSBridgeThread extends Thread
	{
		// Attributes
		private DestinationResolver destinationResolver = new SessionDestinationResolver();
		
		// Runtime
		private boolean stopRequired;
		private ConnectionFactory sourceConnectionFactory;
		private ConnectionFactory targetConnectionFactory;
		private Connection sourceConnection;
		private Session sourceSession;
		private MessageConsumer sourceConsumer;
		private Connection targetConnection;
		private Session targetSession;
		private MessageProducer targetProducer;
		private boolean debugEnabled = log.isDebugEnabled();
		
		/**
		 * Constructor
		 */
		public JMSBridgeThread()
		{
			super("JMSBridge["+bridgeDefinition.getName()+"]");
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run()
		{
			try
			{
				log.debug("["+bridgeDefinition.getName()+"] JMS bridge thread starting");
				log.trace(bridgeDefinition);
						
				// Lookup connection factories first
				this.sourceConnectionFactory = getConnectionFactory(bridgeDefinition.getSource());
				this.targetConnectionFactory = getConnectionFactory(bridgeDefinition.getTarget());
				
				while (!stopRequired)
				{
					// Receive a message from source
					Message msg = receiveFromSource();
					if (msg == null)
					{
						if (stopRequired)
							break;
						
						failures++;
						log.error("["+bridgeDefinition.getName()+"] Consumer was closed");
						dropSourceResources();
						dropTargetResources();
						continue;
					}

					// Forward it to target
					if (!forwardToTarget(msg))
						break;
					
					// Double phase commit
					try
					{
						if (bridgeDefinition.isCommitSourceFirst())
						{
							// Source
							if (bridgeDefinition.isConsumerTransacted())
								getSourceSession().commit();
							else
							if (bridgeDefinition.getConsumerAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE)
								msg.acknowledge();
							
							// Target
							if (bridgeDefinition.isProducerTransacted())
								getTargetSession().commit();
						}
						else
						{
							// Target
							if (bridgeDefinition.isProducerTransacted())
								getTargetSession().commit();
							
							// Source
							if (bridgeDefinition.isConsumerTransacted())
								getSourceSession().commit();
							else
							if (bridgeDefinition.getConsumerAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE)
								msg.acknowledge();
						}
						
						forwardedMessages++;
					}
					catch (JMSException e)
					{
						failures++;
						log.error("["+bridgeDefinition.getName()+"] Double phase commit failed",e);
						dropSourceResources();
						dropTargetResources();
					}
					
					if (debugEnabled)
						log.debug("["+bridgeDefinition.getName()+"] Forwarded message : "+msg);
				}
			}
			catch (Throwable e)
			{
				log.fatal("["+bridgeDefinition.getName()+"] JMSBridge thread failed",e);
			}
			finally
			{
				dropSourceResources();
				dropTargetResources();
				
				log.debug("["+bridgeDefinition.getName()+"] JMS bridge thread exiting");
			}
		}
		
		private Message receiveFromSource()
		{
			while (!stopRequired)
			{
				try
				{
					MessageConsumer consumer = getSourceConsumer();
					if (consumer == null)
						return null;
					
					Message msg = consumer.receive();
					if (msg == null)
					{
						if (stopRequired)
							break;
						
						log.error("Consumer was unexpectedly closed, restarting bridge.");
						dropSourceResources();
						retryWait();
					}
					else
						return msg; // OK
				}
				catch (JMSException e)
				{
					failures++;
					log.error("["+bridgeDefinition.getName()+"] Receive failed",e);
					dropSourceResources();
					retryWait();
				}
			}
			return null;
		}
			
		private boolean forwardToTarget( Message message )
		{
			while (!stopRequired)
			{
				try
				{
					MessageProducer producer = getTargetProducer();
					if (producer == null)
						break;

					// Check TTL
					long TTL = 0;
					if (message.getJMSExpiration() > 0)
					{
						long now = System.currentTimeMillis();
						if (now >= message.getJMSExpiration())
						{
							log.warn("Message "+message.getJMSMessageID()+" has expired, discarding it.");
							return true;
						}
						else
							TTL = message.getJMSExpiration() - now;
					}
	
					producer.send(message,bridgeDefinition.getProducerDeliveryMode(),message.getJMSPriority(),TTL);
					return true;
				}
				catch (JMSException e)
				{
					failures++;
					log.error("["+bridgeDefinition.getName()+"] Send failed",e);
					dropTargetResources();
					retryWait();
				}
			}
			
			return false;
		}
		
		public synchronized void pleaseStop()
		{
			stopRequired = true;
			notify();
			dropSourceResources();
		}
		
		private ConnectionFactory getConnectionFactory( PeerDescriptor peer ) throws JMSException
	    {
	        try
	        {
	            Context context = JNDITools.getContext(peer.getJdniInitialContextFactoryName(),peer.getProviderURL(),null);
	            return (ConnectionFactory)context.lookup(peer.getJndiConnectionFactoryName());
	        }
	        catch (NamingException e)
	        {
	            throw new JMSException("JNDI error : "+e.toString());
	        }
	    }
		
		private synchronized MessageConsumer getSourceConsumer()
		{
			if (sourceConsumer == null)
			{
				while (!stopRequired)
				{
					try
					{
						Session sourceSession = getSourceSession();
						if (sourceSession == null)
							break;
						
						Destination source = destinationResolver.getDestination(bridgeDefinition.getSource(), bridgeDefinition.getSourceDestination(), sourceSession);
						sourceConsumer = sourceSession.createConsumer(source);
						getSourceConnection().start();
						break;
					}
					catch (JMSException e)
					{
						failures++;
						log.error("["+bridgeDefinition.getName()+"] Cannot create consumer on source queuer",e);
						dropSourceResources();
						retryWait();
					}
				}
			}
			return sourceConsumer;
		}
		
		private synchronized MessageProducer getTargetProducer()
		{
			if (targetProducer == null)
			{
				while (!stopRequired)
				{
					try
					{
						Session targetSession = getTargetSession();
						if (targetSession == null)
							break;
						
						Destination target = destinationResolver.getDestination(bridgeDefinition.getTarget(), bridgeDefinition.getTargetDestination(), targetSession);
						targetProducer = targetSession.createProducer(target);
						break;
					}
					catch (JMSException e)
					{
						failures++;
						log.error("["+bridgeDefinition.getName()+"] Cannot create producer on target queuer",e);
						dropTargetResources();
						retryWait();
					}
				}
			}
			return targetProducer;
		}
		
		private synchronized Session getTargetSession()
		{
			if (targetSession == null)
			{
				while (!stopRequired)
				{
					try
					{
						Connection targetConnection = getTargetConnection();
						if (targetConnection == null)
							break;
						
						targetSession = targetConnection.createSession(bridgeDefinition.isProducerTransacted(),
								                                       bridgeDefinition.isProducerTransacted() ? Session.SESSION_TRANSACTED : Session.AUTO_ACKNOWLEDGE);
						break;
					}
					catch (JMSException e)
					{
						failures++;
						log.error("["+bridgeDefinition.getName()+"] Cannot create session on target queuer",e);
						dropTargetResources();
						retryWait();
					}
				}
			}
			return targetSession;
		}
		
		private synchronized Session getSourceSession()
		{
			if (sourceSession == null)
			{
				while (!stopRequired)
				{
					try
					{
						Connection sourceConnection = getSourceConnection();
						if (sourceConnection == null)
							break;
							
						sourceSession = sourceConnection.createSession(bridgeDefinition.isConsumerTransacted(),
 								                                       bridgeDefinition.isConsumerTransacted() ? Session.SESSION_TRANSACTED : bridgeDefinition.getConsumerAcknowledgeMode());
						break;
					}
					catch (JMSException e)
					{
						failures++;
						log.error("["+bridgeDefinition.getName()+"] Cannot create session on source queuer",e);
						dropSourceResources();
						retryWait();
					}
				}
			}
			return sourceSession;
		}

		private synchronized Connection getTargetConnection()
		{
			if (targetConnection == null)
			{
				while (!stopRequired)
				{
					try
					{
						targetConnection = targetConnectionFactory.createConnection(bridgeDefinition.getTarget().getUserName(),bridgeDefinition.getTarget().getPassword());
						break;
					}
					catch (JMSException e)
					{
						failures++;
						log.error("["+bridgeDefinition.getName()+"] Cannot create connection to target queuer",e);
						dropTargetResources();
						retryWait();
					}
				}
			}
			return targetConnection;
		}

		private synchronized Connection getSourceConnection()
		{
			if (sourceConnection == null)
			{
				while (!stopRequired)
				{
					try
					{
						sourceConnection = sourceConnectionFactory.createConnection(bridgeDefinition.getSource().getUserName(),bridgeDefinition.getSource().getPassword());
						break;
					}
					catch (JMSException e)
					{
						failures++;
						log.error("["+bridgeDefinition.getName()+"] Cannot create connection to source queuer",e);
						dropSourceResources();
						retryWait();
					}
				}
			}
			return sourceConnection;
		}
		
		private synchronized void retryWait()
		{
			if (stopRequired)
				return;
				
			log.error("["+bridgeDefinition.getName()+"] Waiting "+bridgeDefinition.getRetryInterval()+" second(s) before retrying");
			try
			{
				wait(bridgeDefinition.getRetryInterval()*1000L);
			}
			catch (InterruptedException e)
			{
				log.error("["+bridgeDefinition.getName()+"] Retry wait was interrupted");
			}
		}
		
		private synchronized void dropSourceResources()
		{
			try
			{
				if (sourceConsumer != null)
					sourceConsumer.close();
			}
			catch (Exception e)
			{
				log.error("["+bridgeDefinition.getName()+"] Could not close source consumer",e);
			}
			finally
			{
				sourceConsumer = null;
			}
			
			try
			{
				if (sourceSession != null)
					sourceSession.close();
			}
			catch (Exception e)
			{
				log.error("["+bridgeDefinition.getName()+"] Could not close source session",e);
			}
			finally
			{
				sourceSession = null;
			}
			
			try
			{
				if (sourceConnection != null)
					sourceConnection.close();
			}
			catch (Exception e)
			{
				log.error("["+bridgeDefinition.getName()+"] Could not close source connection",e);
			}
			finally
			{
				sourceConnection = null;
			}
		}
		
		private synchronized void dropTargetResources()
		{
			try
			{
				if (targetProducer != null)
					targetProducer.close();
			}
			catch (Exception e)
			{
				log.error("["+bridgeDefinition.getName()+"] Could not close target producer",e);
			}
			finally
			{
				targetProducer = null;
			}
			
			try
			{
				if (targetSession != null)
					targetSession.close();
			}
			catch (Exception e)
			{
				log.error("["+bridgeDefinition.getName()+"] Could not close target session",e);
			}
			finally
			{
				targetSession = null;
			}
			
			try
			{
				if (targetConnection != null)
					targetConnection.close();
			}
			catch (Exception e)
			{
				log.error("["+bridgeDefinition.getName()+"] Could not close target connection",e);
			}
			finally
			{
				targetConnection = null;
			}
		}
	}
}
