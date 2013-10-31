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
package net.timewalker.ffmq4.listeners.utils;

import java.util.ArrayList;
import java.util.List;

import net.timewalker.ffmq4.common.message.AbstractMessage;
import net.timewalker.ffmq4.local.destination.notification.NotificationProxy;
import net.timewalker.ffmq4.transport.PacketTransport;
import net.timewalker.ffmq4.transport.packet.NotificationPacket;
import net.timewalker.ffmq4.utils.id.IntegerID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private List<NotificationPacket> notificationBuffer = new ArrayList<>();
    
    /**
     * Constructor
     */
    public RemoteNotificationProxy( IntegerID sessionId , PacketTransport transport )
    {
        this.sessionId = sessionId;
        this.transport = transport;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.notification.NotificationProxy#sendNotification(net.timewalker.ffmq4.utils.id.IntegerID, net.timewalker.ffmq4.common.message.AbstractMessage)
     */
    @Override
	public synchronized void addNotification(IntegerID consumerId, AbstractMessage prefetchedMessage)
    {
		NotificationPacket notifPacket = new NotificationPacket();
    	notifPacket.setSessionId(sessionId);
        notifPacket.setConsumerId(consumerId);
        notifPacket.setMessage(prefetchedMessage);
        
        notificationBuffer.add(notifPacket);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.local.destination.notification.NotificationProxy#flush()
     */
    @Override
	public synchronized void flush()
    {
        if (!transport.isClosed())
        {
            try
            {
                int len = notificationBuffer.size();
                for (int i = 0 ; i < len ; i++)
                {
                	NotificationPacket notifPacket = notificationBuffer.get(i);
                	notifPacket.setDonePrefetching(i == len-1);
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
