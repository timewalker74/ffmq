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
package net.timewalker.ffmq4.storage.message.impl;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;

import net.timewalker.ffmq4.common.message.AbstractMessage;
import net.timewalker.ffmq4.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq4.storage.data.LinkedDataStore;
import net.timewalker.ffmq4.storage.data.impl.InMemoryLinkedDataStore;

/**
 * InMemoryMessageStore
 */
public final class InMemoryMessageStore extends AbstractMessageStore
{
    /**
     * Constructor
     */
    public InMemoryMessageStore( QueueDefinition queueDef )
    {
        super(queueDef);
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.storage.message.impl.AbstractMessageStore#createDataStore()
     */
    @Override
	protected LinkedDataStore createDataStore()
    {
        int maxSize = queueDef.getMaxNonPersistentMessages();
        return new InMemoryLinkedDataStore(queueDef.getName()+" Volatile Store",
                                           Math.min(maxSize, 16),
                                           maxSize);
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.storage.message.MessageStore#getDeliveryMode()
     */
    @Override
	public int getDeliveryMode()
    {
    	return DeliveryMode.NON_PERSISTENT;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.storage.message.impl.AbstractMessageStore#retrieveMessage(int)
     */
    @Override
	protected AbstractMessage retrieveMessage(int handle) throws JMSException
    {
    	return (AbstractMessage)((InMemoryLinkedDataStore)dataStore).retrieve(handle);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.storage.message.impl.AbstractMessageStore#retrieveMessagePriority(int)
     */
    @Override
	protected int retrieveMessagePriority(int handle) throws JMSException
    {
    	return retrieveMessage(handle).getJMSPriority();
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.store.impl.AbstractMessageStore#storeMessage(net.timewalker.ffmq4.common.message.AbstractMessage, int)
     */
    @Override
	protected int storeMessage(AbstractMessage message, int previousHandle) throws JMSException
    {
    	return ((InMemoryLinkedDataStore)dataStore).store(message, previousHandle);
	}
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.storage.message.impl.AbstractMessageStore#replaceMessage(int, net.timewalker.ffmq4.common.message.AbstractMessage)
     */
    @Override
	protected int replaceMessage(int handle, AbstractMessage message) throws JMSException
    {
    	return ((InMemoryLinkedDataStore)dataStore).replace(handle,message);
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.store.MessageStore#delete()
     */
    @Override
	public void delete() throws JMSException
    {
        // Nothing to do (volatile)
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.storage.message.MessageStore#isFailSafe()
     */
    @Override
	public boolean isFailSafe()
    {
    	return false;
    }
}
