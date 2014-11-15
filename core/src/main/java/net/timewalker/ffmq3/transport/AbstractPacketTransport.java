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
package net.timewalker.ffmq3.transport;


/**
 * <p>Base class for a packet transport</p>
 */
public abstract class AbstractPacketTransport implements PacketTransport
{
	// Attributes
	protected String id;
	protected PacketTransportListener listener;
	protected boolean client;
	
    // Runtime
	protected Object closeLock = new Object();
	protected boolean closed = false;
	
	/**
	 * Constructor
	 */
	public AbstractPacketTransport( String id , boolean client )
	{
		this.id = id;
		this.client = client;	
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.PacketTransport#getId()
	 */
	public final String getId()
    {
        return id;
    }
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.PacketTransport#isClosed()
	 */
	public boolean isClosed()
	{
		return closed;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.PacketTransport#setListener(net.timewalker.ffmq3.transport.PacketTransportListener)
	 */
	public void setListener(PacketTransportListener listener)
	{
		this.listener = listener;
	}
}
