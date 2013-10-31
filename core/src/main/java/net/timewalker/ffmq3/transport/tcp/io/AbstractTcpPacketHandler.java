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
package net.timewalker.ffmq3.transport.tcp.io;

import net.timewalker.ffmq3.transport.PacketTransportListener;
import net.timewalker.ffmq3.utils.watchdog.ActiveObject;

/**
 * <p>Base class for threads handling the outgoing/incoming traffic on a TCP socket</p>
 */
public abstract class AbstractTcpPacketHandler implements ActiveObject
{
	// Attributes
    protected String id;
    protected PacketTransportListener listener;
    protected long lastActivity; // Time of the last received/transmitted packet
    
    /**
     * Constructor
     */
    protected AbstractTcpPacketHandler( String id , PacketTransportListener listener )
    {
        this.id = id;
        this.listener = listener;
        this.lastActivity = System.currentTimeMillis();
    }
    
    /**
     * Get the handler id
     * @return the handler id
     */
    public final String getId()
    {
        return id;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.watchdog.ActiveObject#getLastActivity()
     */
	public final long getLastActivity()
	{
		return lastActivity;
	}
}
