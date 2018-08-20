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
package net.timewalker.ffmq3.local.destination.subscription;

import javax.jms.Topic;

import net.timewalker.ffmq3.common.message.MessageSelector;
import net.timewalker.ffmq3.common.message.selector.SelectorIndexKey;
import net.timewalker.ffmq3.local.destination.LocalQueue;
import net.timewalker.ffmq3.local.destination.LocalTopic;
import net.timewalker.ffmq3.local.session.LocalMessageConsumer;

/**
 * <p>Internal holder of a durable {@link Topic} subscription, to be used by the {@link LocalTopic} implementation.</p>
 */
public final class LocalTopicSubscription
{
    private long creationTS;
    private String subscriberId;
    private MessageSelector messageSelector;
    private String connectionID;
    private boolean durable;
    private boolean noLocal;
    
    // Runtime
    private SelectorIndexKey indexKey;
    private LocalQueue localQueue;
    
    /**
     * Constructor
     */
    public LocalTopicSubscription( LocalMessageConsumer consumer )
    {
        this.creationTS = System.currentTimeMillis();
        this.subscriberId = consumer.getSubscriberId();
        this.messageSelector = consumer.getParsedSelector();
        this.connectionID = consumer.getSession().getConnection().getId();
        this.localQueue = consumer.getLocalQueue();
        this.durable = consumer.isDurable();
        this.noLocal = consumer.getNoLocal();
    }

    /**
     * @return the creationTS
     */
    public long getCreationTS()
    {
        return creationTS;
    }

    /**
     * @return the messageSelector
     */
    public MessageSelector getMessageSelector()
    {
        return messageSelector;
    }

    /**
     * @return the subscriberID
     */
    public String getSubscriberId()
    {
        return subscriberId;
    }

    /**
     * @return the localQueue
     */
    public LocalQueue getLocalQueue()
    {
        return localQueue;
    }

    /**
     * @return the connectionID
     */
    public String getConnectionID()
    {
        return connectionID;
    }

    /**
	 * @return the durable
	 */
	public boolean isDurable()
	{
		return durable;
	}
    
    /**
     * @return the noLocal
     */
    public boolean getNoLocal()
    {
        return noLocal;
    }
    
    /**
	 * @param indexKey the indexKey to set
	 */
	public void setIndexKey(SelectorIndexKey indexKey)
	{
		this.indexKey = indexKey;
	}
	
	/**
	 * @return the indexKey
	 */
	public SelectorIndexKey getIndexKey()
	{
		return indexKey;
	}
	
	public boolean isIndexed()
	{
		return indexKey != null;
	}
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("LocalTopicSubscription[subscriberID=");
        sb.append(subscriberId);
        sb.append(",connectionID=");
        sb.append(connectionID);
        sb.append(",selector=");
        sb.append(messageSelector);
        if (indexKey != null)
        {
        	sb.append(",indexKey=");
            sb.append(indexKey);
        }
        sb.append(",durable=");
        sb.append(durable);
        sb.append(",noLocal=");
        sb.append(noLocal);
        sb.append(",creationTS=");
        sb.append(creationTS);
        sb.append("] using ");
        sb.append(localQueue);
        
        return sb.toString();
    }
}
