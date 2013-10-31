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
package net.timewalker.ffmq3.transport.tcp.nio;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * NIOClientSocketHandler
 */
public interface NIOClientSocketHandler
{
	/**
	 * Get the unique client identifier
	 * @return the unique client identifier
	 */
	public String getId();
	
	/**
	 * Get the associated socket channel
	 * @return the associated socket channel
	 */
	public SocketChannel getSocketChannel();
	
	/**
	 * Get this client's input buffer
	 * @return this client's input buffer
	 */
	public ByteBuffer getInputBuffer();
	
	/**
	 * Get this client's output buffer
	 * @return this client's output buffer
	 */
	public ByteBuffer getOutputBuffer();
	
	/**
	 * Process incoming data available in the input buffer
	 * @return false on error causing the multiplexer to drop the client handler
	 */
	public boolean handleIncomingData();

	/**
	 * Append some outgoing data to the output buffer
	 * @return false on error causing the multiplexer to drop the client handler
	 */
	public boolean appendOutgoingData();
	
	/**
	 * Test if this client has things to sends
	 * @return true if this client has things to sends
	 */
	public boolean hasWriteInterest();
	
	/**
	 * Called if the multiplexer closes the socket channel after a network error
	 */
	public void onSocketChannelClosed();
}
