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
package net.timewalker.ffmq4.local.destination;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Topic;

import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.FFMQSubscriberPolicy;
import net.timewalker.ffmq4.common.message.AbstractMessage;
import net.timewalker.ffmq4.common.message.MessageSelector;
import net.timewalker.ffmq4.local.MessageLockSet;
import net.timewalker.ffmq4.local.destination.subscription.LocalTopicSubscription;
import net.timewalker.ffmq4.local.session.LocalMessageConsumer;
import net.timewalker.ffmq4.local.session.LocalSession;
import net.timewalker.ffmq4.management.destination.definition.TopicDefinition;
import net.timewalker.ffmq4.storage.data.DataStoreFullException;
import net.timewalker.ffmq4.storage.message.MessageSerializationLevel;
import net.timewalker.ffmq4.utils.Committable;
import net.timewalker.ffmq4.utils.ErrorTools;
import net.timewalker.ffmq4.utils.concurrent.CopyOnWriteList;
import net.timewalker.ffmq4.utils.concurrent.SynchronizationBarrier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Implementation for a local JMS {@link Topic}</p>
 */
public final class LocalTopic extends AbstractLocalDestination implements Topic, LocalTopicMBean
{    
    private static final Log log = LogFactory.getLog(LocalTopic.class);
    
    // Definition
    private TopicDefinition topicDef;
    
    // Subscribers map
    private CopyOnWriteList<LocalTopicSubscription> subscriptions = new CopyOnWriteList<>();
    private Map<String,LocalTopicSubscription> subscriptionMap = new Hashtable<>();
    
    // Stats
    private AtomicLong sentToTopicCount = new AtomicLong();
    private AtomicLong dispatchedFromTopicCount = new AtomicLong();
    
    // Runtime
    private Set<Committable> committables = new HashSet<>();
    private boolean pendingChanges;
    
    /**
     * Constructor
     */
    public LocalTopic( TopicDefinition topicDef )
    {
        super(topicDef);
        this.topicDef = topicDef;
    }
        
    /**
     * Get the queue definition
     */
    public TopicDefinition getDefinition()
    {
        return topicDef;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.Topic#getTopicName()
     */
    @Override
	public String getTopicName()
    {
        return getName();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.AbstractLocalDestination#registerConsumer(net.timewalker.ffmq4.local.session.LocalMessageConsumer)
     */
    @Override
	public void registerConsumer(LocalMessageConsumer consumer)
    {
        super.registerConsumer(consumer);
        synchronized (subscriptionMap)
		{
        	LocalTopicSubscription subscription = subscriptionMap.remove(consumer.getSubscriberId());
        	if (subscription == null)
        	{
        		// New subscription
	        	subscription = new LocalTopicSubscription(consumer);
	        	subscriptions.add(subscription);
	        	subscriptionMap.put(consumer.getSubscriberId(),subscription);
        	}
        	else
        	{
        		// Remove old subscription
        		subscriptions.remove(subscription);
        		// Replace by new subscription
        		subscription = new LocalTopicSubscription(consumer);
	        	subscriptions.add(subscription);
	        	subscriptionMap.put(consumer.getSubscriberId(),subscription);
        	}
		}
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.AbstractLocalDestination#unregisterConsumer(net.timewalker.ffmq4.local.session.LocalMessageConsumer)
     */
    @Override
	public void unregisterConsumer(LocalMessageConsumer consumer)
    {
        super.unregisterConsumer(consumer);
        if (!consumer.isDurable())
        {
            log.debug("Removing non-durable subscription "+consumer.getSubscriberId());
            synchronized (subscriptionMap)
    		{
            	LocalTopicSubscription subscription = subscriptionMap.remove(consumer.getSubscriberId());
            	if (subscription != null)
            		subscriptions.remove(subscription);
    		}
        }
    }

    /**
     * Unsubscribe all durable consumers for a given client ID and subscription name
     */ 
    public void unsubscribe( String clientID , String subscriptionName  ) throws JMSException
    {
    	String subscriberID = clientID+"-"+subscriptionName;
    	
    	LocalTopicSubscription subscription = subscriptionMap.get(subscriberID);
    	if (subscription == null)
    		return;
    	
    	if (isConsumerRegistered(subscriberID))
    		throw new FFMQException("Subscription "+subscriptionName+" is still in use","SUBSCRIPTION_STILL_IN_USE");
    		
    	synchronized (subscriptionMap)
		{
        	subscriptionMap.remove(subscriberID);
       		subscriptions.remove(subscription);
		}
    }
        
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.AbstractLocalDestination#putLocked(net.timewalker.ffmq4.common.message.AbstractMessage, net.timewalker.ffmq4.local.session.LocalSession, net.timewalker.ffmq4.local.MessageLockSet)
     */
    @Override
	public boolean putLocked( AbstractMessage srcMessage, LocalSession session , MessageLockSet locks ) throws JMSException
    {
    	checkNotClosed();
    	checkTransactionLock();
    
    	// Check delivery mode
    	if (!topicDef.supportDeliveryMode(srcMessage.getJMSDeliveryMode()))
    		throw new FFMQException("Topic does not support this delivery mode : "+
    				(srcMessage.getJMSDeliveryMode() == DeliveryMode.NON_PERSISTENT ? 
                    "DeliveryMode.NON_PERSISTENT" : "DeliveryMode.PERSISTENT"),
                    "INVALID_DELIVERY_MODE");
    	
    	sentToTopicCount.incrementAndGet();
    	
        String connectionID = session.getConnection().getId();
        CopyOnWriteList<LocalTopicSubscription> subscriptionsSnapshot;
        synchronized (subscriptionMap)
        {
        	if (subscriptions.isEmpty())
        		return false;
        	
        	subscriptionsSnapshot = subscriptions.fastCopy();
        }
        
        boolean commitRequired = false;
        for (int i = 0; i < subscriptionsSnapshot.size(); i++)
		{
    		LocalTopicSubscription subscription = subscriptionsSnapshot.get(i);
            
            // No-local filtering
            if (subscription.getNoLocal() && subscription.getConnectionID().equals(connectionID))
                continue;

            try
            {
                // Message selector filtering
                MessageSelector selector = subscription.getMessageSelector();
                if (selector != null)
                {
                	srcMessage.ensureDeserializationLevel(MessageSerializationLevel.ALL_HEADERS);
                	if (!selector.matches(srcMessage))
                		continue;
                }
                
                LocalQueue subscriberQueue = subscription.getLocalQueue();
                
                // Only use transactional mode for fail-safe durable subscriptions
                if (subscriberQueue.requiresTransactionalUpdate() && subscription.isDurable())
                {
                	if (committables.add(subscriberQueue))
                		subscriberQueue.openTransaction();
                		
                	if (!subscriberQueue.putLocked(srcMessage, session, locks))
                		if (srcMessage.getJMSDeliveryMode() == DeliveryMode.PERSISTENT)
                			throw new IllegalStateException("Should require a commit");
                	
                	pendingChanges = true;
                	commitRequired = true;
                }
                else
                {
                	if (subscriberQueue.putLocked(srcMessage, session, locks))
                		throw new IllegalStateException("Should not require a commit");
                }
            	
            	dispatchedFromTopicCount.incrementAndGet();
            }
            catch (DataStoreFullException e)
            {
            	processPutError(subscription.getSubscriberId(), e, getDefinition().getSubscriberOverflowPolicy());
            }
            catch (JMSException e)
            {
            	processPutError(subscription.getSubscriberId(), e, getDefinition().getSubscriberFailurePolicy());
            }
        }
        
        return commitRequired;
    }
    
    private void processPutError( String subscriberId , JMSException e , int policy ) throws JMSException
    {
    	if ((policy & FFMQSubscriberPolicy.SUBSCRIBER_POLICY_LOG) > 0)
    		ErrorTools.log("subscriber="+subscriberId, e, log);
    	
    	if ((policy & FFMQSubscriberPolicy.SUBSCRIBER_POLICY_REPORT_TO_PRODUCER) > 0)
    		throw e;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.LocalDestinationMBean#getSize()
     */
    @Override
	public int getSize()
    {
    	int size = 0;
    	synchronized (subscriptionMap)
        {
    		for (int i = 0; i < subscriptions.size(); i++)
    		{
        		LocalTopicSubscription subscription = subscriptions.get(i);
                size += subscription.getLocalQueue().getSize();
            }
        }
    	return size;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.LocalDestinationMBean#resetStats()
     */
    @Override
	public void resetStats()
    {
    	super.resetStats();
    	sentToTopicCount.set(0);
    	dispatchedFromTopicCount.set(0);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.LocalTopicMBean#getSentToTopicCount()
     */
    @Override
	public long getSentToTopicCount()
    {
        return sentToTopicCount.get();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.LocalTopicMBean#getDispatchedFromTopicCount()
     */
    @Override
	public long getDispatchedFromTopicCount()
    {
        return dispatchedFromTopicCount.get();
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
       StringBuffer sb = new StringBuffer();
       
       sb.append("Topic{");
       sb.append(getName());
       sb.append("}[size=");
       sb.append(getSize());
       sb.append(",consumers=");
       sb.append(localConsumers.size());
       sb.append(",in=");
       sb.append(sentToTopicCount);
       sb.append(",out=");
       sb.append(dispatchedFromTopicCount);
       sb.append("]");

       return sb.toString();
    }

    public String getConsumersSummary()
    {
        StringBuffer sb = new StringBuffer();
        
        synchronized (subscriptionMap)
        {
            for (int i = 0; i < subscriptions.size(); i++)
            {
                LocalTopicSubscription subscription = subscriptions.get(i);
                
                if (i>0)
                    sb.append("\n");
                sb.append(subscription);
            }
        }
        
        return sb.toString();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.AbstractLocalDestination#hasTransactionSupport()
     */
    @Override
    protected boolean requiresTransactionalUpdate()
    {
    	return topicDef.hasPersistentStore();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.AbstractLocalDestination#hasPendingChanges()
     */
    @Override
    protected boolean hasPendingChanges()
    {
    	return pendingChanges;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.AbstractLocalDestination#close()
     */
    @Override
	public final void close() throws JMSException
    {
    	synchronized (closeLock)
		{
	    	if (closed)
	    		return;
	    	closed = true;
		}
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.Committable#commitChanges(net.timewalker.ffmq4.utils.concurrent.SynchronizationBarrier)
     */
    @Override
    public void commitChanges(SynchronizationBarrier barrier) throws JMSException
    {
    	checkNotClosed();
    	checkTransactionLock();
    	
    	if (!committables.isEmpty())
        {
    		long start = System.currentTimeMillis();
    		
    		Iterator<Committable> allCommitables = committables.iterator();
        	while (allCommitables.hasNext())
        	{
        		Committable committable = allCommitables.next();
        		committable.commitChanges(barrier);
        	}
        	
        	long end = System.currentTimeMillis();
        	notifyCommitTime(end-start);
        	
        	pendingChanges = false;
        }
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.AbstractLocalDestination#closeTransaction()
     */
    @Override
    public void closeTransaction()
    {
    	// Unlock subscriptions queues first
    	if (!committables.isEmpty())
        {
	    	Iterator<Committable> allCommitables = committables.iterator();
	    	while (allCommitables.hasNext())
	    	{
	    		Committable committable = allCommitables.next();
	    		committable.closeTransaction();
	    	}
	    	committables.clear();
        }
    	
    	super.closeTransaction();
    }
}
