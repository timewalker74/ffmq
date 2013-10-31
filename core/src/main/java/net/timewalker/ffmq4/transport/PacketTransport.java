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
package net.timewalker.ffmq4.transport;

import net.timewalker.ffmq4.transport.packet.AbstractPacket;

/**
 * PacketTransport
 */
public interface PacketTransport
{
    /**
     * Get the packet transport id
     * @return the packet transport id
     */
    public String getId();
    
    /**
     * Start the transport layer
     */
    public void start() throws PacketTransportException;
    
    /**
     * Stop and close the transport layer
     */
    public void close();
    
    /**
     * Test if the transport is closed
     */
    public boolean isClosed();
    
    /**
     * Send a packet on this transport
     */
    public void send( AbstractPacket packet ) throws PacketTransportException;

    /**
     * Test if send operation should be throttled down to avoid send queue overflow
     */
    public boolean needsThrottling();
    
    /**
	 * @param listener the listener to set
	 */
	public void setListener(PacketTransportListener listener);
	
	/**
	 * Get an ID representing the transport remote peer
	 * @return an ID representing the remote peer
	 */
	public String getRemotePeerID();
}
