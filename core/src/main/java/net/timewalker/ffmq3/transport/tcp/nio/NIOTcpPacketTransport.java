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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import net.timewalker.ffmq3.FFMQCoreSettings;
import net.timewalker.ffmq3.transport.PacketTransportException;
import net.timewalker.ffmq3.transport.packet.AbstractPacket;
import net.timewalker.ffmq3.transport.packet.PacketSerializer;
import net.timewalker.ffmq3.transport.packet.query.PingQuery;
import net.timewalker.ffmq3.transport.tcp.AbstractTcpPacketTransport;
import net.timewalker.ffmq3.transport.tcp.SocketUtils;
import net.timewalker.ffmq3.utils.RawDataBuffer;
import net.timewalker.ffmq3.utils.Settings;
import net.timewalker.ffmq3.utils.watchdog.ActiveObject;
import net.timewalker.ffmq3.utils.watchdog.ActivityWatchdog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * NIOTcpPacketTransport
 */
public final class NIOTcpPacketTransport extends AbstractTcpPacketTransport implements NIOClientSocketHandler
{
	protected static final Log log = LogFactory.getLog(NIOTcpPacketTransport.class);

	// Attributes
	private ByteBuffer inputBuffer;
	private ByteBuffer outputBuffer;
	private NIOTcpMultiplexer multiplexer;
	private SocketChannel socketChannel;
	private int sendQueueMaxSize;
	private int maxPacketSize;
	
	// Runtime
	private LinkedList sendQueue = new LinkedList();
	private RawDataBuffer packetOutputBuffer;
	private RawDataBuffer packetInputBuffer;
	private int currentInputOffset;
	private int currentOutputOffset;
	protected long lastSendActivity;
	protected long lastRecvActivity;
	private ActiveObject sendActivityMonitor;
	private ActiveObject recvActivityMonitor;
	private boolean trustedConnection = false;
	private boolean traceEnabled;
	
	/**
	 * Constructor
	 */
	public NIOTcpPacketTransport( String id , NIOTcpMultiplexer multiplexer , URI transportURI , Settings settings ) throws PacketTransportException
	{
		super(id,true,settings);
		this.multiplexer = multiplexer;
		init(settings);
		this.socketChannel = connect(transportURI);
	}
	
	/**
	 * Constructor
	 */
	public NIOTcpPacketTransport( String id , NIOTcpMultiplexer multiplexer , SocketChannel socketChannel , Settings settings )
	{
		super(id,false,settings);
		this.multiplexer = multiplexer;
		this.socketChannel = socketChannel;
		init(settings);
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.tcp.AbstractTcpPacketTransport#getRemotePeer()
	 */
	public SocketAddress getRemotePeer()
	{
		return socketChannel.socket().getRemoteSocketAddress();
	}
	
	private void init( Settings settings )
	{
		this.traceEnabled = log.isTraceEnabled();
		
		this.sendQueueMaxSize = settings.getIntProperty(FFMQCoreSettings.TRANSPORT_TCP_SEND_QUEUE_MAX_SIZE, 1000);
		this.maxPacketSize = client ? -1 : settings.getIntProperty(FFMQCoreSettings.TRANSPORT_TCP_PACKET_MAX_SIZE, 1024*1024+1024);
		
		// Stream buffers
		this.inputBuffer = ByteBuffer.allocate(streamRecvBufferSize).order(ByteOrder.BIG_ENDIAN);
		this.outputBuffer = ByteBuffer.allocate(streamSendBufferSize).order(ByteOrder.BIG_ENDIAN);
		
		// Packet buffers
		this.packetInputBuffer = new RawDataBuffer(initialPacketBufferSize);
		this.packetOutputBuffer = new RawDataBuffer(initialPacketBufferSize);
		
		this.lastSendActivity = this.lastRecvActivity = System.currentTimeMillis();
		if (client)
		{
			sendActivityMonitor = new ActiveObject() {
				/* (non-Javadoc)
				 * @see net.timewalker.ffmq3.utils.watchdog.ActiveObject#getLastActivity()
				 */
				public long getLastActivity()
				{
					return lastSendActivity;
				}
				
				/* (non-Javadoc)
				 * @see net.timewalker.ffmq3.utils.watchdog.ActiveObject#getTimeoutDelay()
				 */
				public long getTimeoutDelay()
				{
					return pingInterval*1000L;
				}
				
				/* (non-Javadoc)
				 * @see net.timewalker.ffmq3.utils.watchdog.ActiveObject#onActivityTimeout()
				 */
				public boolean onActivityTimeout() throws Exception
				{
					send(new PingQuery());
					return false;
				}
			};
			ActivityWatchdog.getInstance().register(sendActivityMonitor);
		}
		recvActivityMonitor = new ActiveObject() {
			/* (non-Javadoc)
			 * @see net.timewalker.ffmq3.utils.watchdog.ActiveObject#getLastActivity()
			 */
			public long getLastActivity()
			{
				return lastRecvActivity;
			}
			
			/* (non-Javadoc)
			 * @see net.timewalker.ffmq3.utils.watchdog.ActiveObject#getTimeoutDelay()
			 */
			public long getTimeoutDelay()
			{
				return pingInterval*1000L*2;
			}
			
			/* (non-Javadoc)
			 * @see net.timewalker.ffmq3.utils.watchdog.ActiveObject#onActivityTimeout()
			 */
			public boolean onActivityTimeout() throws Exception
			{
				log.warn(getId()+" : ping timeout on client socket, closing connection.");
				closeTransport(true);
				return true;
			}
		};
		ActivityWatchdog.getInstance().register(recvActivityMonitor);
	}
	
	/**
     * Connect the transport to its remote endpoint
     */
    private SocketChannel connect( URI transportURI ) throws PacketTransportException
    {
        String host = transportURI.getHost();
        int port = transportURI.getPort();
        
        try
        {
        	SocketChannel socketChannel = SocketChannel.open();
        	socketChannel.configureBlocking(false);
        	SocketUtils.setupSocket(socketChannel.socket(),
        			                socketSendBufferSize,
        			                socketRecvBufferSize);
        	        	
            log.debug("#"+id+" opening a TCP connection to "+host+":"+port);
            socketChannel.connect(new InetSocketAddress(host,port));
            
            return socketChannel;
        }
        catch (Exception e)
        {
            log.error("#"+id+" could not connect to "+host+":"+port,e);
            throw new PacketTransportException("Could not connect to "+host+":"+port+" : "+e.toString());
        }
    }
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.tcp.nio.NIOClientSocketHandler#getInputBuffer()
	 */
	public ByteBuffer getInputBuffer()
	{
		return inputBuffer;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.tcp.nio.NIOClientSocketHandler#getOutputBuffer()
	 */
	public ByteBuffer getOutputBuffer()
	{
		return outputBuffer;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.tcp.nio.NIOClientSocketHandler#getSocketChannel()
	 */
	public SocketChannel getSocketChannel()
	{
		return socketChannel;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.tcp.nio.NIOClientSocketHandler#handleIncomingData()
	 */
	public boolean handleIncomingData()
	{
		lastRecvActivity = System.currentTimeMillis();
		
		while (inputBuffer.remaining() > 0)
		{		
			if (packetInputBuffer.size() == 0)
			{
				if (inputBuffer.remaining() >= 4) // packet_size = sizeof(int) = 4
				{
					int packetSize = inputBuffer.getInt();
					
					// Security check
					int actualMaxPacketSize = Integer.MAX_VALUE;
					if (maxPacketSize != -1)
						actualMaxPacketSize = trustedConnection ? maxPacketSize : 1024;
					
					if (packetSize > actualMaxPacketSize)
					{
						log.error("#"+id+" packet is too large : "+packetSize+" (maxPacketSize="+actualMaxPacketSize+"), dropping client.");
						return false;
					}
					
					packetInputBuffer.setSize(packetSize);
				}
				else
					return true; // Not enough data to proceed ...
			}
			else
			{
				// Append to current packet
				int readAmount = Math.min(inputBuffer.remaining(), packetInputBuffer.size() - currentInputOffset);
				packetInputBuffer.getFrom(inputBuffer, currentInputOffset, readAmount);
				currentInputOffset += readAmount;
				
				// Packet is complete ?
				if (currentInputOffset == packetInputBuffer.size())
				{
					AbstractPacket packet = unserializePacket(packetInputBuffer);
					if (packet == null)
						return false; // Invalid packet
					packetInputBuffer.clear();
					currentInputOffset = 0;
					
					if (traceEnabled)
	                    log.trace("#"+id+" Received "+packet);
					
					if (listener != null)
						trustedConnection = listener.packetReceived(packet);
				}
			}
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.tcp.nio.NIOClientSocketHandler#appendOutgoingData()
	 */
	public boolean appendOutgoingData()
	{
		while (outputBuffer.remaining() > 0)
		{
			if (packetOutputBuffer.size() == 0)
			{
				if (sendQueue.size() == 0)
					return true; // No more packet to send
				
				if (outputBuffer.remaining() < 4) // packet_size = sizeof(int) = 4
					return true; // Not enough space to proceed
				
				// Write packet size
				if (!serializePacket((AbstractPacket)sendQueue.getFirst(),packetOutputBuffer))
					return false; // Invalid packet
				outputBuffer.putInt(packetOutputBuffer.size());
				currentOutputOffset = 0;
			}
			else
			{
				int writeAmount = Math.min(outputBuffer.remaining(),packetOutputBuffer.size() - currentOutputOffset);
				packetOutputBuffer.putTo(outputBuffer, currentOutputOffset, writeAmount);
				currentOutputOffset += writeAmount;
				
				// Packet completly sent ?
				if (currentOutputOffset == packetOutputBuffer.size())
				{
					AbstractPacket sentPacket;
					synchronized (sendQueue)
					{
						sentPacket = (AbstractPacket)sendQueue.removeFirst();
					}
					packetOutputBuffer.clear();
					
					if (listener != null)
						listener.packetSent(sentPacket);
				}
			}
		}
		return true;
	}
	
	private boolean serializePacket( AbstractPacket packet , RawDataBuffer buffer )
	{
		try
		{
			buffer.clear();
			PacketSerializer.serializeTo(packet,buffer);
			return true;
		}
		catch (Exception e)
		{
			log.error("#"+id+" cannot unserialize packet",e);
			return false;
		}
	}
	
	private AbstractPacket unserializePacket( RawDataBuffer buffer )
	{
		try
		{
			buffer.reset();
			return PacketSerializer.unserializeFrom(buffer);
		}
		catch (Exception e)
		{
			log.error("#"+id+" cannot unserialize packet",e);
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.tcp.nio.NIOClientSocketHandler#hasWriteInterest()
	 */
	public boolean hasWriteInterest()
	{
		synchronized (sendQueue)
		{
			return sendQueue.size() > 0;
		}
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.PacketTransport#send(net.timewalker.ffmq3.transport.packet.AbstractPacket)
	 */
	public void send(AbstractPacket packet) throws PacketTransportException
	{
		if (closed)
			throw new PacketTransportException("Transport is closed");
		
		if (packet.isResponseExpected())
			lastSendActivity = System.currentTimeMillis();
		
		boolean wakeUpRequired;
		synchronized (sendQueue)
		{
			wakeUpRequired = sendQueue.isEmpty();
			sendQueue.add(packet);
		}
		
		if (wakeUpRequired)
			multiplexer.wakeUp();
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.PacketTransport#needsThrottling()
	 */
	public boolean needsThrottling()
	{
		synchronized (sendQueue)
		{
			return (sendQueueMaxSize > 0 && sendQueue.size() >= sendQueueMaxSize);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.PacketTransport#start()
	 */
	public void start() throws PacketTransportException
	{
		if (client)
			multiplexer.registerClientSocketHandler(this);
		else
			multiplexer.wakeUp();
	}
	
	protected void closeTransport( boolean linkFailed )
	{
		synchronized (closeLock)
		{
			if (closed)
	            return; // Already closed
			closed = true;
		}
		
		if (sendActivityMonitor != null)
			ActivityWatchdog.getInstance().unregister(sendActivityMonitor);
		if (recvActivityMonitor != null)
			ActivityWatchdog.getInstance().unregister(recvActivityMonitor);
		
    	// Close the socket
		if (!linkFailed)
			multiplexer.unregisterClientSocketHandler(this);

		// Notify listener
		if (listener != null)
			listener.transportClosed(linkFailed);
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.tcp.nio.NIOClientSocketHandler#onSocketChannelClosed()
	 */
	public void onSocketChannelClosed()
	{
		closeTransport(true);
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.PacketTransport#close()
	 */
	public void close()
	{
		closeTransport(false);
	}	
}
