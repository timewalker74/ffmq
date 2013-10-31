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
package net.timewalker.ffmq3.storage.message.impl;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;

import net.timewalker.ffmq3.common.message.AbstractMessage;
import net.timewalker.ffmq3.common.message.MessageSerializer;
import net.timewalker.ffmq3.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq3.storage.data.LinkedDataStore;
import net.timewalker.ffmq3.storage.data.impl.AbstractBlockBasedDataStore;
import net.timewalker.ffmq3.storage.data.impl.BlockBasedDataStore;
import net.timewalker.ffmq3.storage.data.impl.BlockBasedDataStoreTools;
import net.timewalker.ffmq3.storage.data.impl.JournalingBlockBasedDataStore;
import net.timewalker.ffmq3.utils.async.AsyncTaskManager;

/**
 * BlockFileMessageStore
 */
public final class BlockFileMessageStore extends AbstractMessageStore
{
    // Attributes
    private AsyncTaskManager asyncTaskManager;
	private boolean useJournal;
	
	/**
     * Constructor
     */
    public BlockFileMessageStore( QueueDefinition queueDef ,
    		                      AsyncTaskManager asyncTaskManager )
    {
        super(queueDef);
        this.asyncTaskManager = asyncTaskManager;
        this.useJournal = queueDef.isUseJournal() && !queueDef.isTemporary();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.message.impl.AbstractMessageStore#createDataStore()
     */
    @Override
	protected LinkedDataStore createDataStore()
    {
        if (useJournal)
            return new JournalingBlockBasedDataStore(queueDef,asyncTaskManager);
        else
            return new BlockBasedDataStore(queueDef);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.message.MessageStore#getDeliveryMode()
     */
    @Override
	public int getDeliveryMode()
    {
    	return DeliveryMode.PERSISTENT;
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.message.impl.AbstractMessageStore#retrieveMessage(int)
     */
    @Override
	protected AbstractMessage retrieveMessage(int handle) throws JMSException
    {
    	byte[] rawMsg = (byte[])dataStore.retrieve(handle);
    	return MessageSerializer.unserialize(rawMsg, true);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.message.impl.AbstractMessageStore#retrieveMessagePriority(int)
     */
    @Override
	protected int retrieveMessagePriority(int handle) throws JMSException
    {
    	// Only read the first header bytes of the message to read the priority field
    	byte[] msgHeader = ((AbstractBlockBasedDataStore)dataStore).retrieveHeader(handle, 2);    	
    	return msgHeader[1] & 0x0F;
    }
    
    private byte[] serialize( AbstractMessage message )
    {
		return MessageSerializer.serialize(message,((AbstractBlockBasedDataStore)dataStore).getBlockSize());
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.message.impl.AbstractMessageStore#replaceMessage(int, net.timewalker.ffmq3.common.message.AbstractMessage)
     */
    @Override
	protected int replaceMessage(int handle, AbstractMessage message) throws JMSException
    {
        return dataStore.replace(handle,serialize(message));
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.store.impl.AbstractMessageStore#storeMessage(net.timewalker.ffmq3.common.message.AbstractMessage, int)
     */
    @Override
	protected int storeMessage(AbstractMessage message, int previousHandle) throws JMSException
    {
        return dataStore.store(serialize(message), previousHandle);
	}
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.store.MessageStore#delete()
     */
    @Override
	public void delete() throws JMSException
    {
        BlockBasedDataStoreTools.delete(queueDef.getName(), 
                                        queueDef.getDataFolder(), 
                                        false);  
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.storage.message.MessageStore#isFailSafe()
     */
    @Override
	public boolean isFailSafe()
    {
    	return useJournal;
    }
}
