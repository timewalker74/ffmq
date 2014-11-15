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

package net.timewalker.ffmq3.transport.tcp;

import java.net.SocketAddress;

import net.timewalker.ffmq3.FFMQCoreSettings;
import net.timewalker.ffmq3.transport.AbstractPacketTransport;
import net.timewalker.ffmq3.utils.Settings;

/**
 * <p>Base class for a TCP-based packet transport</p>
 */
public abstract class AbstractTcpPacketTransport extends AbstractPacketTransport
{
	// Attributes
	protected int streamSendBufferSize;
	protected int streamRecvBufferSize;
	protected int initialPacketBufferSize;
	protected int socketSendBufferSize;
	protected int socketRecvBufferSize;
	protected int sendQueueMaxSize;
	public int pingInterval;
	
	/**
	 * Constructor
	 */
	public AbstractTcpPacketTransport( String id , boolean client , Settings settings )
	{
		super(id,client);

		this.streamSendBufferSize  = settings.getIntProperty(FFMQCoreSettings.TRANSPORT_TCP_STREAM_SEND_BUFFER_SIZE,8192);	
    	this.streamRecvBufferSize  = settings.getIntProperty(FFMQCoreSettings.TRANSPORT_TCP_STREAM_RECV_BUFFER_SIZE,8192);
    	this.initialPacketBufferSize = settings.getIntProperty(FFMQCoreSettings.TRANSPORT_TCP_INITIAL_PACKET_BUFFER_SIZE,4096);	    	
    	this.socketSendBufferSize = settings.getIntProperty(FFMQCoreSettings.TRANSPORT_TCP_SOCKET_SEND_BUFFER_SIZE,65536);	
    	this.socketRecvBufferSize = settings.getIntProperty(FFMQCoreSettings.TRANSPORT_TCP_SOCKET_RECV_BUFFER_SIZE,65536);    	
    	this.sendQueueMaxSize = settings.getIntProperty(FFMQCoreSettings.TRANSPORT_TCP_SEND_QUEUE_MAX_SIZE, 100);
		this.pingInterval = settings.getIntProperty(FFMQCoreSettings.TRANSPORT_TCP_PING_INTERVAL, 30);
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.PacketTransport#getRemotePeerID()
	 */
	public final String getRemotePeerID()
	{
		SocketAddress peerAddress = getRemotePeer();
		return peerAddress != null ? peerAddress.toString() : "not connected";
	}
	
	/**
	 * Get the remote peer address
	 * @return the remote peer address (may return null if not available)
	 */
	public abstract SocketAddress getRemotePeer();
}
