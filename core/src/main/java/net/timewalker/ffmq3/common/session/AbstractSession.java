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
package net.timewalker.ffmq3.common.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.common.connection.AbstractConnection;
import net.timewalker.ffmq3.common.destination.QueueRef;
import net.timewalker.ffmq3.common.destination.TopicRef;
import net.timewalker.ffmq3.common.message.BytesMessageImpl;
import net.timewalker.ffmq3.common.message.EmptyMessageImpl;
import net.timewalker.ffmq3.common.message.MapMessageImpl;
import net.timewalker.ffmq3.common.message.ObjectMessageImpl;
import net.timewalker.ffmq3.common.message.StreamMessageImpl;
import net.timewalker.ffmq3.common.message.TextMessageImpl;
import net.timewalker.ffmq3.local.destination.LocalQueue;
import net.timewalker.ffmq3.local.destination.LocalTopic;
import net.timewalker.ffmq3.utils.concurrent.locks.ReadWriteLock;
import net.timewalker.ffmq3.utils.concurrent.locks.ReentrantReadWriteLock;
import net.timewalker.ffmq3.utils.id.IntegerID;
import net.timewalker.ffmq3.utils.id.IntegerIDProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Base implementation for a JMS {@link Session}</p>
 */
public abstract class AbstractSession implements Session
{
    private static final Log log = LogFactory.getLog(AbstractSession.class);
    
    // Parent connection
    protected AbstractConnection connection;
    
    // Attributes
    protected IntegerID id;
    protected boolean transacted;
    protected int acknowledgeMode;
    protected boolean closed;
    public Object deliveryLock = new Object();
    
    // Children
    protected Map consumersMap = new Hashtable();
    private Map producersMap = new Hashtable();
    private Map browsersMap = new Hashtable();
    
    // Runtime
    protected IntegerIDProvider idProvider = new IntegerIDProvider();
    protected ReadWriteLock externalAccessLock = new ReentrantReadWriteLock();
    
    /**
     * Constructor
     */
    public AbstractSession( AbstractConnection connection , boolean transacted , int acknowledgeMode )
    {
        this.connection = connection;
        this.transacted = transacted;
        this.acknowledgeMode = acknowledgeMode;
    }
    
    /**
     * Constructor
     */
    public AbstractSession( IntegerID id , AbstractConnection connection , boolean transacted , int acknowledgeMode )
    {
        this(connection,transacted,acknowledgeMode);
        this.id = id;
    }
    
    /**
     * Get the session id
     * @return the id
     */
    public final IntegerID getId()
    {
        return id;
    }

    /**
     * Check that the session is not closed
     */
    protected final void checkNotClosed() throws JMSException
    {
        if (closed)
            throw new IllegalStateException("Session is closed"); // [JMS SPEC]
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.Session#close()
     */
    public final void close() throws JMSException
    {
    	externalAccessLock.writeLock().lock();
    	try
		{
	        if (closed)
	            return;
	        closed = true;
	        onSessionClose();
		}
    	finally
    	{
    		externalAccessLock.writeLock().unlock();
    	}
    	onSessionClosed();
    }

    protected void onSessionClose()
    {
    	connection.unregisterSession(this);
	    closeRemainingConsumers();
	    closeRemainingProducers();
	    closeRemainingBrowsers();
    }

    protected void onSessionClosed()
    {
        // Nothing
    }
    
    /**
     * Check temporary destinations scope (JMS Spec 4.4.3 p2)
     * @param destination destination to check
     */
    public final void checkTemporaryDestinationScope( Destination destination ) throws JMSException
    {
    	if (destination instanceof LocalQueue)
    	{
    		LocalQueue localQueue = (LocalQueue)destination; 
	        if (localQueue.isTemporary() && !connection.isRegisteredTemporaryQueue(localQueue.getQueueName()))
	       		throw new IllegalStateException("Temporary queue does not belong to session's connection.");
    	}
    	else
    	if (destination instanceof LocalTopic)
    	{	
    		LocalTopic localTopic = (LocalTopic)destination; 
	        if (localTopic.isTemporary() && !connection.isRegisteredTemporaryTopic(localTopic.getTopicName()))
	       		throw new IllegalStateException("Temporary topic does not belong to session's connection.");
    	}
    	else
    		throw new FFMQException("Unexpected destination type : "+destination,"INTERNAL_ERROR");
    }
    
    /**
     * Wake up all children consumers
     */
    public final void wakeUpConsumers() throws JMSException
    {
        synchronized (consumersMap)
        {
            Iterator allConsumers = consumersMap.values().iterator();
            while (allConsumers.hasNext())
            {
                AbstractMessageConsumer consumer = (AbstractMessageConsumer)allConsumers.next();
                consumer.wakeUp();
            }
        }
    }
    
    /**
     * Lookup a registered consumer
     */
    public final AbstractMessageConsumer lookupRegisteredConsumer( IntegerID consumerId )
    {
        return (AbstractMessageConsumer)consumersMap.get(consumerId);
    }
    
    /**
     * Lookup a registered browser
     */
    public final AbstractQueueBrowser lookupRegisteredBrowser( IntegerID browserId )
    {
        return (AbstractQueueBrowser)browsersMap.get(browserId);
    }
    
    /**
     * Register a consumer
     */
    protected final void registerConsumer( AbstractMessageConsumer consumer )
    {
        if (consumersMap.put(consumer.getId(),consumer) != null)
        	throw new IllegalArgumentException("Consumer "+consumer.getId()+" already exists");
    }
    
    /**
     * Register a producer
     */
    protected final void registerProducer( AbstractMessageProducer producer )
    {
        if (producersMap.put(producer.getId(),producer) != null)
        	throw new IllegalArgumentException("Producer "+producer.getId()+" already exists");
    }
    
    /**
     * Register a browser
     */
    protected final void registerBrowser( AbstractQueueBrowser browser )
    {
    	if (browsersMap.put(browser.getId(),browser) != null)
    		throw new IllegalArgumentException("Browser "+browser.getId()+" already exists");
    }
    
    /**
     * Unregister a consumer
     */
    protected final void unregisterConsumer( AbstractMessageConsumer consumerToRemove )
    {
        if (consumersMap.remove(consumerToRemove.getId()) == null)
            log.warn("Unknown consumer : "+consumerToRemove);
    }
    
    /**
     * Unregister a producer
     */
    protected final void unregisterProducer( AbstractMessageProducer producerToRemove )
    {
        if (producersMap.remove(producerToRemove.getId()) == null)
            log.warn("Unknown producer : "+producerToRemove);
    }
    
    /**
     * Unregister a browser
     */
    protected final void unregisterBrowser( AbstractQueueBrowser browserToRemove )
    {
        if (browsersMap.remove(browserToRemove.getId()) == null)
            log.warn("Unknown browser : "+browserToRemove);
    }
    
    /**
     * Close remaining consumers
     */
    private void closeRemainingConsumers()
    {
        List consumersToClose = new ArrayList(consumersMap.size());
        synchronized (consumersMap)
        {
            consumersToClose.addAll(consumersMap.values());
        }
        for (int n = 0 ; n < consumersToClose.size() ; n++)
        {
            MessageConsumer consumer = (MessageConsumer)consumersToClose.get(n);
            log.debug("Auto-closing unclosed consumer : "+consumer);
            try
            {
                consumer.close();
            }
            catch (Exception e)
            {
                log.error("Could not close consumer "+consumer,e);
            }
        }
    }
    
    /**
     * Close remaining consumers
     */
    private void closeRemainingProducers()
    {
        List producersToClose = new ArrayList(producersMap.size());
        synchronized (producersMap)
        {
            producersToClose.addAll(producersMap.values());
        }
        for (int n = 0 ; n < producersToClose.size() ; n++)
        {
            MessageProducer producer = (MessageProducer)producersToClose.get(n);
            log.debug("Auto-closing unclosed producer : "+producer);
            try
            {
                producer.close();
            }
            catch (Exception e)
            {
                log.error("Could not close producer "+producer,e);
            }
        }
    }
    
    /**
     * Close remaining browsers
     */
    private void closeRemainingBrowsers()
    {
        List browsersToClose = new ArrayList(browsersMap.size());
        synchronized (browsersMap)
        {
        	browsersToClose.addAll(browsersMap.values());
        }
        for (int n = 0 ; n < browsersToClose.size() ; n++)
        {
        	QueueBrowser browser = (QueueBrowser)browsersToClose.get(n);
            log.debug("Auto-closing unclosed browser : "+browser);
            try
            {
                browser.close();
            }
            catch (Exception e)
            {
                log.error("Could not close browser "+browser,e);
            }
        }
    }
    
    /**
     * Acknowledge the given message
     */
    public abstract void acknowledge() throws JMSException;
    
    /*
     * (non-Javadoc)
     * @see javax.jms.Session#createMessage()
     */
    public final Message createMessage() throws JMSException
    {
        return new EmptyMessageImpl();
    }
    
    /* (non-Javadoc)
     * @see javax.jms.Session#createBytesMessage()
     */
    public final BytesMessage createBytesMessage() throws JMSException
    {
        return new BytesMessageImpl();
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createMapMessage()
     */
    public final MapMessage createMapMessage() throws JMSException
    {
        return new MapMessageImpl();
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createObjectMessage()
     */
    public final ObjectMessage createObjectMessage() throws JMSException
    {
        return new ObjectMessageImpl();
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createStreamMessage()
     */
    public final StreamMessage createStreamMessage() throws JMSException
    {
        return new StreamMessageImpl();
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createTextMessage()
     */
    public final TextMessage createTextMessage() throws JMSException
    {
        return new TextMessageImpl();
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.Session#createQueue(java.lang.String)
     */
    public Queue createQueue(String queueName) throws JMSException
    {
        return new QueueRef(queueName);
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#getAcknowledgeMode()
     */
    public final int getAcknowledgeMode() throws JMSException
    {
        if (transacted)
            return Session.SESSION_TRANSACTED; // [JMS Spec]
        return acknowledgeMode;
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#getTransacted()
     */
    public final boolean getTransacted() throws JMSException
    {
        return transacted;
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createObjectMessage(java.io.Serializable)
     */
    public final ObjectMessage createObjectMessage(Serializable object) throws JMSException
    {
        return new ObjectMessageImpl(object);
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createTextMessage(java.lang.String)
     */
    public final TextMessage createTextMessage(String text) throws JMSException
    {
        return new TextMessageImpl(text);
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#getMessageListener()
     */
    public final MessageListener getMessageListener() throws JMSException
    {
        throw new FFMQException("Unsupported feature","UNSUPPORTED_FEATURE");
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#setMessageListener(javax.jms.MessageListener)
     */
    public final void setMessageListener(MessageListener listener) throws JMSException
    {
        throw new FFMQException("Unsupported feature","UNSUPPORTED_FEATURE");
    }
 
    /* (non-Javadoc)
     * @see javax.jms.Session#createTopic(java.lang.String)
     */
    public Topic createTopic(String topicName) throws JMSException
    {
        return new TopicRef(topicName);
    }
    
    /* (non-Javadoc)
     * @see javax.jms.Session#createConsumer(javax.jms.Destination, java.lang.String)
     */
    public final MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException
    {
        return createConsumer(destination,messageSelector,false);
    }

    /* (non-Javadoc)
     * @see javax.jms.Session#createConsumer(javax.jms.Destination)
     */
    public final MessageConsumer createConsumer(Destination destination) throws JMSException
    {
        return createConsumer(destination,null,false);
    }
    
    /* (non-Javadoc)
     * @see javax.jms.Session#createDurableSubscriber(javax.jms.Topic, java.lang.String)
     */
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException
    {
        return createDurableSubscriber(topic,name,null,false);
    }
    
    /* (non-Javadoc)
     * @see javax.jms.Session#createBrowser(javax.jms.Queue)
     */
    public QueueBrowser createBrowser(Queue queue) throws JMSException
    {
        return createBrowser(queue,null);
    }
    
    /* (non-Javadoc)
     * @see javax.jms.Session#run()
     */
    public final void run()
    {
        // Not implemented
    }

    /**
     * @return the connection
     */
    public final AbstractConnection getConnection()
    {
        return connection;
    }
    
    /**
     * Get the number of active producers for this session
     * @return the number of active producers for this session
     */
    public final int getConsumersCount()
    {
    	return consumersMap.size();
    }
    
    /**
     * Get the number of active producers for this session
     * @return the number of active producers for this session
     */
    public final int getProducersCount()
    {
    	return producersMap.size();
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("Session[#");
        sb.append(id);
        sb.append("](");
        if (transacted)
            sb.append("transacted");
        else
        {
            sb.append("not transacted, acknowledgeMode=");
            sb.append(acknowledgeMode);
        }
        sb.append(")");
        
        return sb.toString();
    }
    
    /**
	 * Get a description of entities held by this object
	 */
	public final void getEntitiesDescription( StringBuffer sb )
	{
		sb.append(toString());
		sb.append("{");
		synchronized (consumersMap)
		{
			if (!consumersMap.isEmpty())
			{
				int pos = 0;
				Iterator consumers = consumersMap.values().iterator();
				while (consumers.hasNext())
				{
					AbstractMessageHandler handler = (AbstractMessageHandler)consumers.next();
					if (pos++ > 0)
						sb.append(",");					
					handler.getEntitiesDescription(sb);
				}
			}
		}
		synchronized (producersMap)
		{
			if (!producersMap.isEmpty())
			{
				int pos = 0;
				Iterator producers = producersMap.values().iterator();
				while (producers.hasNext())
				{
					AbstractMessageHandler handler = (AbstractMessageHandler)producers.next();
					if (pos++ > 0)
						sb.append(",");					
					handler.getEntitiesDescription(sb);
				}
			}
		}
		sb.append("}");
	}

	public final void waitForDeliverySync()
	{
		synchronized (deliveryLock)
		{
			// Just waiting for lock ...
		}
	}
}
