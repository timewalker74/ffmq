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
package net.timewalker.ffmq3.local;

import javax.jms.Session;

import net.timewalker.ffmq3.common.message.AbstractMessage;
import net.timewalker.ffmq3.local.destination.LocalQueue;

/**
 * <p>Item of a {@link TransactionSet}. Keeps track of a message lock obtained by a JMS {@link Session} in the current transaction.</p>
 */
public final class TransactionItem
{
	// Attributes
	private int handle;
	private String messageId;
	private int deliveryMode;
	private LocalQueue destination;
	
	/**
	 * Constructor
	 */
	public TransactionItem( int handle,
	                        String messageID,
	                        int deliveryMode,
	                        LocalQueue destination )
	{
		this.handle = handle;
		this.messageId = messageID;
		this.deliveryMode = deliveryMode;
		this.destination = destination;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.local.StoredMessageReference#getHandle()
	 */
	public int getHandle()
	{
		return handle;
	}
	
	/**
	 * @param handle the handle to set
	 */
	public void setHandle(int handle)
	{
		this.handle = handle;
	}
	
	public String getMessageId()
	{
		return messageId;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.local.StoredMessageReference#getDeliveryMode()
	 */
	public int getDeliveryMode()
	{
		return deliveryMode;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.local.StoredMessageReference#getDestination()
	 */
	public LocalQueue getDestination()
	{
		return destination;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[TransactionItem] handle=");
		sb.append(handle);
		sb.append(" messageID=");
		sb.append(messageId);
		sb.append(" destination=");
		sb.append(destination);
		sb.append(" deliveryMode=");
		sb.append(deliveryMode);
		
		return sb.toString();
	}
	
	public MessageLock toMessageLock( AbstractMessage message )
	{
		return new MessageLock(handle, deliveryMode, destination, message);
	}
}
