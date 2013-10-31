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
package net.timewalker.ffmq4.transport.tcp.io;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import net.timewalker.ffmq4.transport.PacketTransportListener;
import net.timewalker.ffmq4.transport.packet.AbstractPacket;
import net.timewalker.ffmq4.transport.packet.PacketSerializer;
import net.timewalker.ffmq4.transport.packet.query.PingQuery;
import net.timewalker.ffmq4.utils.RawDataBuffer;
import net.timewalker.ffmq4.utils.SerializationTools;
import net.timewalker.ffmq4.utils.watchdog.ActivityWatchdog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Thread handling the outgoing traffic of a TCP socket.
 * The TcpPacketSender is also responsible for sending keep-alive pings
 * when the connection is idle.</p>
 */
public class TcpPacketSender extends AbstractTcpPacketHandler implements Runnable
{
	private static final Log log = LogFactory.getLog(TcpPacketSender.class);
	
	// Attributes
	private TcpPacketTransport transport;
    private NetworkOutputChannel outChannel;
    private int pingInterval;
    private int sendQueueMaxSize;
    
    // Runtime
    private LinkedList<AbstractPacket> sendQueue = new LinkedList<>();
    private LinkedList<AbstractPacket> pipeline = new LinkedList<>();
    private Semaphore waitLock = new Semaphore(0);
    private volatile boolean stopRequired;
    
    /**
     * Constructor
     */
    protected TcpPacketSender(TcpPacketTransport transport , NetworkOutputChannel outputChannel , PacketTransportListener listener, int pingInterval, int sendQueueMaxSize )
    {
        super(transport.getId(),listener);
        this.transport = transport;
        this.outChannel = outputChannel;
        this.pingInterval = pingInterval;
        if (pingInterval > 0)
        	ActivityWatchdog.getInstance().register(this);
        this.sendQueueMaxSize = sendQueueMaxSize;
    }

    /**
     * Send a packet
     */
    public void send(AbstractPacket packet)
    {
    	synchronized (sendQueue)
		{    		
    		sendQueue.addLast(packet);
		}
    	waitLock.release();
    }
    
    public boolean needsThrottling()
    {
    	synchronized (sendQueue)
		{
    		return (sendQueueMaxSize > 0 && sendQueue.size() >= sendQueueMaxSize);
		}
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
	public void run()
    {
    	try
    	{
    		while (!stopRequired)
    		{
	    		waitLock.acquire();
	    		if (stopRequired)
	    			break;
	    		
	    		// Implementation notes :
	    		//-----------------------
	    		//   As Socket TCP_NODELAY is set to minimize latency,
	    		//   socket buffers are sent ASAP in TCP packets.
	    		//   In order to fill more data in actual packets we try 
	    		//   to pipeline all immediately available messages 
	    		//   before asking to flush the buffers.

	    		// De-queue pending messages and move them to the pipeline
	    		synchronized (sendQueue)
                {
                    while (!sendQueue.isEmpty() && pipeline.size() < 16)
                    {
                        AbstractPacket packet = sendQueue.removeFirst();
                        pipeline.add(packet);
                    }
                }
	    			   
	    		if (pipeline.size() > 0)
	    		{	    		    
    	    		// Write all pipelined packets to the socket buffered output stream
    	    		while (pipeline.size() > 0)
    	    		{
    	    		    AbstractPacket packet = pipeline.removeFirst();
    	    		    
    	    		    // We need to serialize the packet in a side buffer in order to 
    	    		    // know its final size before writing it to the actual output stream
    	                RawDataBuffer buffer = outChannel.ioBuffer;
    	                buffer.clear();
    	                PacketSerializer.serializeTo(packet, buffer);
    	                
    	                // Write it on the stream
    	                OutputStream out = outChannel.socketOutputStream;
    	                SerializationTools.writeInt(buffer.size(),out); // Packet size
    	                buffer.writeTo(out); // Packet body
    	    		}
    	    		
    	    		// Flush output stream
    	    		outChannel.flush();
    	    		
    	    		// Update last activity timestamp
    	    		lastActivity = System.currentTimeMillis();
	    		}
    		}
    	}
    	catch (Exception e)
        {
            if (!stopRequired)
            {
            	log.error("#"+id+" transport failed : "+e.toString());
            	transport.closeTransport(true);
            }
        }
        catch (Throwable e)
        {
        	log.fatal("#"+id+" TCP packet receiver died",e);
        }
        
        log.debug("#"+id+" stopping.");
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.watchdog.ActiveObject#getTimeoutDelay()
     */
    @Override
	public long getTimeoutDelay()
    {
    	return pingInterval*1000L;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.watchdog.ActiveObject#onActivityTimeout()
     */
    @Override
	public boolean onActivityTimeout() throws Exception
    {
    	try
    	{
    		send(new PingQuery());
    		return false;
    	}
    	catch (Exception e)
    	{
    		log.warn("#"+id+" cannot send ping to server : "+e.toString());
    		return true;
    	}
    }
    
    public void pleaseStop()
    {
        stopRequired = true;
        waitLock.release();
        if (pingInterval > 0)
        	ActivityWatchdog.getInstance().unregister(this);
    }
}
