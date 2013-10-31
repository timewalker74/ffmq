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

import net.timewalker.ffmq3.common.message.AbstractMessage;
import net.timewalker.ffmq3.local.destination.LocalQueue;

/**
 * <p>Immutable message lock reference</p>
 */
public final class MessageLock
{
	// Attributes
	private int handle;
	private int deliveryMode;
	private LocalQueue destination;
	private AbstractMessage message;
	
	/**
	 * Constructor
	 */
	public MessageLock( int handle,
			            int deliveryMode ,
	                    LocalQueue destination ,
	                    AbstractMessage message )
	{
		this.handle = handle;
		this.deliveryMode = deliveryMode;
		this.destination = destination;
		this.message = message;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.local.StoredMessageReference#getHandle()
	 */
	public int getHandle()
	{
		return handle;
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
	
	/**
	 * @return the message
	 */
	public AbstractMessage getMessage()
	{
		return message;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[MessageLock] handle=");
		sb.append(handle);
		sb.append(" destination=");
		sb.append(destination);
		sb.append(" deliveryMode=");
		sb.append(deliveryMode);
		sb.append(" msgId=");
		sb.append(message.getJMSMessageID());
		
		return sb.toString();
	}
	
	public TransactionItem toTransactionItem()
	{
		return new TransactionItem(handle, message.getJMSMessageID(), deliveryMode, destination);
	}
}
