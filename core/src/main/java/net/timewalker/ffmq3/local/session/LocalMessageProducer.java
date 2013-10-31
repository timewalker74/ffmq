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
package net.timewalker.ffmq3.local.session;

import javax.jms.Destination;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;

import net.timewalker.ffmq3.FFMQConstants;
import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.common.destination.TemporaryDestination;
import net.timewalker.ffmq3.common.message.AbstractMessage;
import net.timewalker.ffmq3.common.message.MessageTools;
import net.timewalker.ffmq3.common.session.AbstractMessageProducer;
import net.timewalker.ffmq3.local.connection.LocalConnection;
import net.timewalker.ffmq3.security.Action;
import net.timewalker.ffmq3.security.Resource;
import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * <p>Implementation of a local JMS {@link MessageProducer}</p>
 */
public class LocalMessageProducer extends AbstractMessageProducer
{
    /**
     * Constructor
     */
    public LocalMessageProducer(LocalSession session,Destination destination,IntegerID producerId) throws JMSException
    {
        super(session,destination,producerId);
        this.session = session;

        // Security : if destination is set at creation time we can check permissions early
        if (destination != null)
        	checkDestinationPermission(destination);
    }
    
    private final void checkDestinationPermission( Destination destination ) throws JMSException
    {
    	if (!(destination instanceof TemporaryDestination))
    	{
    	    boolean adminRequest = false;
            if (destination instanceof Queue)
            {
                String queueName = ((Queue)destination).getQueueName();
                if (queueName.equals(FFMQConstants.ADM_REQUEST_QUEUE))
                {
                    LocalConnection conn = (LocalConnection)session.getConnection();
                    conn.checkPermission(Resource.SERVER, Action.REMOTE_ADMIN);
                    adminRequest = true;
                }
                else
                if (queueName.equals(FFMQConstants.ADM_REPLY_QUEUE))
                {
                    // Only the internal admin thread can produce on this queue
                    LocalConnection conn = (LocalConnection)session.getConnection();
                    if (conn.getSecurityContext() != null)
                        throw new FFMQException("Access denied to administration queue "+queueName,"ACCESS_DENIED");
                    adminRequest = true;
                }
            }
            
            // For all other destinations
            if (!adminRequest)
                ((LocalConnection)session.getConnection()).checkPermission(destination,Action.PRODUCE);
    	}
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.common.session.AbstractMessageProducer#sendToDestination(javax.jms.Destination, boolean, javax.jms.Message, int, int, long)
     */
    protected final void sendToDestination(Destination destination, boolean destinationOverride, Message srcMessage, int deliveryMode, int priority, long timeToLive) throws JMSException
    {
        // Check that the destination was specified
        if (destination == null)
            throw new InvalidDestinationException("Destination not specified");  // [JMS SPEC]

        // Create an internal copy if necessary
        AbstractMessage message = MessageTools.makeInternalCopy(srcMessage);
        
        externalAccessLock.readLock().lock();
        try
		{
    		checkNotClosed();
    		
	        // Dispatch to session
            ((LocalSession)session).dispatch(message);
		}
        finally
        {
        	externalAccessLock.readLock().unlock();
        }
    }
}
