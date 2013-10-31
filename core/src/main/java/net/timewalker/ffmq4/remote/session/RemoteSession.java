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
package net.timewalker.ffmq4.remote.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import net.timewalker.ffmq4.FFMQClientSettings;
import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.client.ClientEnvironment;
import net.timewalker.ffmq4.common.destination.DestinationTools;
import net.timewalker.ffmq4.common.destination.TemporaryQueueRef;
import net.timewalker.ffmq4.common.destination.TemporaryTopicRef;
import net.timewalker.ffmq4.common.message.AbstractMessage;
import net.timewalker.ffmq4.common.message.MessageTools;
import net.timewalker.ffmq4.common.session.AbstractSession;
import net.timewalker.ffmq4.remote.connection.RemoteConnection;
import net.timewalker.ffmq4.storage.data.DataStoreFullException;
import net.timewalker.ffmq4.transport.PacketTransportEndpoint;
import net.timewalker.ffmq4.transport.packet.AbstractQueryPacket;
import net.timewalker.ffmq4.transport.packet.query.AcknowledgeQuery;
import net.timewalker.ffmq4.transport.packet.query.CloseSessionQuery;
import net.timewalker.ffmq4.transport.packet.query.CommitQuery;
import net.timewalker.ffmq4.transport.packet.query.CreateSessionQuery;
import net.timewalker.ffmq4.transport.packet.query.CreateTemporaryQueueQuery;
import net.timewalker.ffmq4.transport.packet.query.CreateTemporaryTopicQuery;
import net.timewalker.ffmq4.transport.packet.query.PutQuery;
import net.timewalker.ffmq4.transport.packet.query.RecoverQuery;
import net.timewalker.ffmq4.transport.packet.query.RollbackQuery;
import net.timewalker.ffmq4.transport.packet.query.UnsubscribeQuery;
import net.timewalker.ffmq4.transport.packet.response.CreateTemporaryQueueResponse;
import net.timewalker.ffmq4.transport.packet.response.CreateTemporaryTopicResponse;
import net.timewalker.ffmq4.utils.ErrorTools;
import net.timewalker.ffmq4.utils.StringTools;
import net.timewalker.ffmq4.utils.id.IntegerID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * RemoteSession
 */
public class RemoteSession extends AbstractSession
{
    private static final Log log = LogFactory.getLog(RemoteSession.class);
    
    // Parent connection
    protected final PacketTransportEndpoint transportEndpoint;
    
    // Settings
    private boolean sendAcksAsync;
    private boolean retryOnQueueFull;
    private long retryTimeout;
    
    // Runtime
    private List<String> deliveredMessageIDs = new Vector<>();
    private Object retryLock = new Object();
    private boolean debugEnabled = log.isDebugEnabled();
    private boolean synchronousAckRequired;
    
    /**
     * Constructor
     */
    public RemoteSession( IntegerID sessionId , RemoteConnection connection , PacketTransportEndpoint transportEndpoint , boolean transacted , int acknowledgeMode )
    {
        super(sessionId,connection,transacted,acknowledgeMode);
        this.transportEndpoint = transportEndpoint;
        
        // Load settings
        this.sendAcksAsync = ClientEnvironment.getSettings().getBooleanProperty(FFMQClientSettings.CONSUMER_SEND_ACKS_ASYNC, true);
        this.retryOnQueueFull = ClientEnvironment.getSettings().getBooleanProperty(FFMQClientSettings.PRODUCER_RETRY_ON_QUEUE_FULL, true);
        this.retryTimeout = ClientEnvironment.getSettings().getLongProperty(FFMQClientSettings.PRODUCER_RETRY_TIMEOUT, 30*1000);        
        log.debug("New remote session ID is "+sessionId);
    }

    /**
     * Initialize the remote endpoint for this session
     */
    public void remoteInit() throws JMSException
    {
        CreateSessionQuery query = new CreateSessionQuery();
        query.setSessionId(id);
        query.setTransacted(transacted);
        query.setAcknowledgeMode(acknowledgeMode);
        transportEndpoint.blockingRequest(query);
    }
    
    public final PacketTransportEndpoint getTransportEndpoint()
    {
        return transportEndpoint;
    }
    
    protected final void dispatch( Message message ) throws JMSException
    {
    	if (debugEnabled)
    		log.debug("#"+id+" Sending message "+message.getJMSMessageID());
    	
    	boolean asyncDispatch = transacted || message.getJMSDeliveryMode() == DeliveryMode.NON_PERSISTENT; 
    			    	
        PutQuery query = new PutQuery();
        query.setSessionId(id);
        
        if (asyncDispatch)
        {
        	// Create a message copy to make sure the message is not modified concurrently
        	Message msgCopy = MessageTools.makeInternalCopy(message);
        	query.setMessage((AbstractMessage)msgCopy);
        }
        else
        	query.setMessage((AbstractMessage)message);

        if (asyncDispatch) 
        	transportEndpoint.nonBlockingRequest(query);
        else
        {
        	if (retryOnQueueFull)
        		retriableBlockingQuery(query, retryTimeout);
        	else
        		transportEndpoint.blockingRequest(query);
        }
    }
    
    /**
     * Add a delivered message ID
     * @param deliveredMessageID
     */
    public final void notifyDeliveredMessage( String deliveredMessageID )
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	    	if (closed)
	    		return;
	    	
	    	if (debugEnabled)
	    		log.debug(this+" Adding delivered message ID : "+deliveredMessageID);
	    	
	    	this.deliveredMessageIDs.add(deliveredMessageID);
	    	if (!transacted && acknowledgeMode != Session.DUPS_OK_ACKNOWLEDGE)
	    		synchronousAckRequired = true;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }
    
    /* (non-Javadoc)
     * @see javax.jms.Session#createBrowser(javax.jms.Queue, java.lang.String)
     */
    @Override
	public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
		    checkNotClosed();
		    RemoteQueueBrowser browser = new RemoteQueueBrowser(idProvider.createID(),
		    		                                            this,
		    		                                            queue,
		    		                                            messageSelector);
		    registerBrowser(browser);
		    browser.remoteInit();
		    return browser;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createConsumer(javax.jms.Destination, java.lang.String, boolean)
     */
    @Override
	public final MessageConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        RemoteMessageConsumer consumer =  new RemoteMessageConsumer(idProvider.createID(),
	        		                                                    this,
			                                                            DestinationTools.asRef(destination),
			                                                            messageSelector,
			                                                            noLocal);
	        registerConsumer(consumer);
	        consumer.remoteInit();
	        return consumer;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createDurableSubscriber(javax.jms.Topic, java.lang.String, java.lang.String, boolean)
     */
    @Override
	public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
    		checkNotClosed();
	        RemoteDurableTopicSubscriber subscriber = new RemoteDurableTopicSubscriber(idProvider.createID(),
	        		                                                                   this,
	                                                                                   topic,
	                                                                                   messageSelector,
	                                                                                   noLocal,
	                                                                                   name);
	        registerConsumer(subscriber);
	        subscriber.remoteInit();
	        return subscriber;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createProducer(javax.jms.Destination)
     */
    @Override
	public final MessageProducer createProducer(Destination destination) throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        RemoteMessageProducer producer = new RemoteMessageProducer(this,
	                                                                   DestinationTools.asRef(destination),
	                                                                   idProvider.createID());
	        registerProducer(producer);
	        return producer;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createTemporaryQueue()
     */
    @Override
	public TemporaryQueue createTemporaryQueue() throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        
	        CreateTemporaryQueueQuery query = new CreateTemporaryQueueQuery();
	        query.setSessionId(id);
	        CreateTemporaryQueueResponse response = (CreateTemporaryQueueResponse)transportEndpoint.blockingRequest(query);
	        
	        return new TemporaryQueueRef(connection,response.getQueueName());
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createTemporaryTopic()
     */
    @Override
	public TemporaryTopic createTemporaryTopic() throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        
	        CreateTemporaryTopicQuery query = new CreateTemporaryTopicQuery();
	        query.setSessionId(id);
	        CreateTemporaryTopicResponse response = 
	            (CreateTemporaryTopicResponse)transportEndpoint.blockingRequest(query);
	        
	        return new TemporaryTopicRef(connection,response.getTopicName());
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#recover()
     */
    @Override
	public final void recover() throws JMSException
    {
    	if (transacted)
            throw new IllegalStateException("Session is transacted"); // [JMS SPEC]
    	
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();

	        RecoverQuery query = new RecoverQuery();
	        query.setSessionId(id);
	        query.setDeliveredMessageIDs(deliveredMessageIDs);
	        transportEndpoint.blockingRequest(query);
	        deliveredMessageIDs.clear();
	        
	        synchronousAckRequired = false;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#unsubscribe(java.lang.String)
     */
    @Override
	public void unsubscribe(String subscriptionName) throws JMSException
    {
    	if (StringTools.isEmpty(subscriptionName))
            throw new FFMQException("Empty subscription name","INVALID_SUBSCRIPTION_NAME");
    	
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        
	        UnsubscribeQuery query = new UnsubscribeQuery();
	        query.setSessionId(id);
	        query.setSubscriptionName(subscriptionName);
	        transportEndpoint.blockingRequest(query);
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.Session#commit()
     */
    @Override
	public final void commit() throws JMSException
    {
    	if (!transacted)
            throw new IllegalStateException("Session is not transacted"); // [JMS SPEC]
    	
    	log.debug("#"+id+" commit()");
    	
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        
	        final CommitQuery query = new CommitQuery();
	        query.setSessionId(id);
	        query.setDeliveredMessageIDs(deliveredMessageIDs);
	        
	        if (retryOnQueueFull)
	        	retriableBlockingQuery(query,retryTimeout);
	        else
		        transportEndpoint.blockingRequest(query);
	      
	        deliveredMessageIDs.clear();
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    private void retriableBlockingQuery( AbstractQueryPacket query , long retryTimeout ) throws JMSException
    {
    	long retryWait = 50; // ms
    	long totalWait = 0;
    	
        while (true)
        {
	        try
	        {
	        	transportEndpoint.blockingRequest(query);
		        break;
	        }
	        catch (DataStoreFullException e)
	        {
	        	if (retryTimeout <= 0 || totalWait < retryTimeout)
	        	{
	        		// Release lock during passive wait
	        		externalAccessLock.readLock().unlock();
	        		try
					{
	        			synchronized (retryLock)
						{
	        				retryLock.wait(retryWait);
						}
					}
					catch (InterruptedException ex)
					{
						// Interrupted --> give up immediatly
						throw e;
					}
					finally
					{
						// Re-acquire lock during passive wait
						externalAccessLock.readLock().lock();
					}
					
					// Concurrently closed ?
        			if (closed)
        				throw new IllegalStateException("Session is closed");
					
        			// Update total wait time
					totalWait += retryWait;
					
					// Exponential wait delay growth
					if (totalWait < retryTimeout)
					{
						retryWait = retryWait*2;
						if (retryWait > 2000)
							retryWait = 2000;
						if (retryWait > retryTimeout-totalWait)
							retryWait = retryTimeout-totalWait;
					}
					
					continue; // Try again
	        	}

	        	// Give up ...
	        	throw e;
	        }
        }
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.Session#rollback()
     */
    @Override
	public final void rollback() throws JMSException
    {
    	if (!transacted)
            throw new IllegalStateException("Session is not transacted"); // [JMS SPEC]
    	
    	log.debug("#"+id+" rollback()");
    	
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        
	        RollbackQuery query = new RollbackQuery();
	        query.setSessionId(id);
	        query.setDeliveredMessageIDs(deliveredMessageIDs);
	        transportEndpoint.blockingRequest(query);
	        deliveredMessageIDs.clear();
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.common.session.AbstractSession#onSessionClose()
     */
    @Override
	protected void onSessionClose()
    {
    	super.onSessionClose();
    	
    	if (retryOnQueueFull)
    	{
	    	synchronized (retryLock)
			{
	    		retryLock.notifyAll();	
			}
    	}
    	
    	try
    	{
        	CloseSessionQuery query = new CloseSessionQuery();
    	    query.setSessionId(id);
    	    transportEndpoint.blockingRequest(query);
    	}
        catch (JMSException e)
        {
            ErrorTools.log(e, log);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.common.session.AbstractSession#onSessionClosed()
     */
    @Override
	protected void onSessionClosed()
    {
        super.onSessionClosed();
        transportEndpoint.close();
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.common.session.AbstractSession#acknowledge()
     */
    @Override
	public final void acknowledge() throws JMSException
    {
    	if (transacted)
            throw new IllegalStateException("Session is transacted"); // [JMS SPEC]
    	
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        if (deliveredMessageIDs.isEmpty())
	        	throw new FFMQException("No received message to acknowledge","INTERNAL_ERROR");
	        
	        if (sendAcksAsync && !synchronousAckRequired)
	        {
	        	// Copy message list
	        	List<String> messageIDs = new ArrayList<>(deliveredMessageIDs.size());
	        	for(int n=0;n<deliveredMessageIDs.size();n++)
	        		messageIDs.add(deliveredMessageIDs.get(n));
	        	deliveredMessageIDs.clear();
	        	
	        	AcknowledgeQuery query = new AcknowledgeQuery();
		        query.setSessionId(id);
		        query.setDeliveredMessageIDs(messageIDs);
		        transportEndpoint.nonBlockingRequest(query);
	        }
	        else
	        {
	        	AcknowledgeQuery query = new AcknowledgeQuery();
		        query.setSessionId(id);
		        query.setDeliveredMessageIDs(deliveredMessageIDs);
		        transportEndpoint.blockingRequest(query);
		        deliveredMessageIDs.clear();
	        }
	        
	        synchronousAckRequired = false;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }
}
