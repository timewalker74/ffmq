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
package net.timewalker.ffmq3.listeners.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.timewalker.ffmq3.common.message.AbstractMessage;
import net.timewalker.ffmq3.local.destination.notification.NotificationProxy;
import net.timewalker.ffmq3.transport.PacketTransport;
import net.timewalker.ffmq3.transport.packet.NotificationPacket;
import net.timewalker.ffmq3.utils.id.IntegerID;

/**
 * NotificationProxy
 */
public final class RemoteNotificationProxy implements NotificationProxy
{
    private static final Log log = LogFactory.getLog(RemoteNotificationProxy.class);
    
    // Attributes
    private PacketTransport transport;
    private IntegerID sessionId;
    
    // Runtime
    private List notificationBuffer = new ArrayList();
    
    /**
     * Constructor
     */
    public RemoteNotificationProxy( IntegerID sessionId , PacketTransport transport )
    {
        this.sessionId = sessionId;
        this.transport = transport;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.notification.NotificationProxy#sendNotification(net.timewalker.ffmq3.utils.id.IntegerID, net.timewalker.ffmq3.common.message.AbstractMessage)
     */
    public synchronized void addNotification(IntegerID consumerId, AbstractMessage prefetchedMessage)
    {
		NotificationPacket notifPacket = new NotificationPacket();
    	notifPacket.setSessionId(sessionId);
        notifPacket.setConsumerId(consumerId);
        notifPacket.setMessage(prefetchedMessage);
        
        // Last packet for this consumer should be flagged as 'done'
        notifPacket.setDonePrefetching(true);
        // Clear 'donePrefetching' of previous packet of same consumer
    	for(int i=notificationBuffer.size()-1;i>=0;i--)
    	{
    		NotificationPacket previousNotifPacket = (NotificationPacket)notificationBuffer.get(i);
    		if (previousNotifPacket.getConsumerId().equals(consumerId))
    		{
    			previousNotifPacket.setDonePrefetching(false);
    			break; // Older packets were already cleared
    		}
    	}
        
        notificationBuffer.add(notifPacket);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.destination.notification.NotificationProxy#flush()
     */
    public synchronized void flush()
    {
        if (!transport.isClosed())
        {
            try
            {
                int len = notificationBuffer.size();
                for (int i = 0 ; i < len ; i++)
                {
                	NotificationPacket notifPacket = (NotificationPacket)notificationBuffer.get(i);
                    transport.send(notifPacket);
                }
            }
            catch (Exception e)
            {
                log.error("Could not send notification packet",e);
                transport.close();
            }
        }
        
        notificationBuffer.clear();
    }
}
