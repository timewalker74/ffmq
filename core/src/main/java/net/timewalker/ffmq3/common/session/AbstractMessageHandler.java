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

import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import net.timewalker.ffmq3.common.connection.AbstractConnection;
import net.timewalker.ffmq3.utils.JavaTools;
import net.timewalker.ffmq3.utils.concurrent.locks.ReadWriteLock;
import net.timewalker.ffmq3.utils.concurrent.locks.ReentrantReadWriteLock;
import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * <p>Base implementation for a message handler (ie. MessageConsumer or MessageProducer)</p>
 */
public abstract class AbstractMessageHandler
{
    // Unique ID
    protected IntegerID id;
    
    // Parents
    protected AbstractConnection connection;
    protected AbstractSession session;
    
    // Destination
    protected Destination destination;
    
    // runtime
    protected boolean closed;
    protected ReadWriteLock externalAccessLock = new ReentrantReadWriteLock();
    
    /**
     * Constructor
     */
    public AbstractMessageHandler( AbstractSession session, 
                                   Destination destination,
                                   IntegerID handlerId )
    {
        this.session = session;
        this.connection = session.getConnection();
        this.destination = destination;
        this.id = handlerId;
    }

    /**
     * Get the message handler unique ID
     */
    public final IntegerID getId()
    {
       return id; 
    }
    
    /**
     * Get the parent session
     */
    public final AbstractSession getSession()
    {
        return session;
    }
    
	/**
     * Check that the session is not closed
     */
    protected final void checkNotClosed() throws JMSException
    {
        if (closed)
            throw new IllegalStateException("Message handler is closed"); // [JMS SPEC]
    }
	
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append(JavaTools.getShortClassName(getClass()));
        sb.append("[#");
        sb.append(id);
        sb.append("]");
        if (destination != null)
        {
        	sb.append("(destination=");
        	sb.append(destination);
        	sb.append(")");
        }
        
        return sb.toString();
    }
    
    /**
	 * Get a description of entities held by this object
	 */
	public final void getEntitiesDescription( StringBuffer sb )
	{
		sb.append(toString());
	}
}
