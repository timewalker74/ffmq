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
package net.timewalker.ffmq3.common.destination;

import javax.jms.JMSException;
import javax.jms.TemporaryQueue;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.common.connection.AbstractConnection;

/**
 * <p>Implementation of a TemporaryQueue reference (not a real queue)</p>
 * @see DestinationRef
 */
public final class TemporaryQueueRef extends QueueRef implements TemporaryQueue, TemporaryDestination
{
	private static final long serialVersionUID = 1L;
	
	// Reference to the parent connection
    private transient AbstractConnection connection;
    
    /**
     * Constructor
     */
    public TemporaryQueueRef( AbstractConnection connection , String queueName )
    {
        super(queueName);
        this.connection = connection;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.TemporaryQueue#delete()
     */
    @Override
	public void delete() throws JMSException
    {
        if (connection == null)
            throw new FFMQException("Temporary queue already deleted","QUEUE_DOES_NOT_EXIST");
            
        connection.deleteTemporaryQueue(name);
        connection = null;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
        return super.toString()+"[T]";
    }
}
