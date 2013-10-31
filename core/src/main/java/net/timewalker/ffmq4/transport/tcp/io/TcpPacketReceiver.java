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

import java.io.IOException;
import java.io.InputStream;

import net.timewalker.ffmq4.transport.PacketTransportException;
import net.timewalker.ffmq4.transport.PacketTransportListener;
import net.timewalker.ffmq4.transport.packet.AbstractPacket;
import net.timewalker.ffmq4.transport.packet.PacketSerializer;
import net.timewalker.ffmq4.utils.RawDataBuffer;
import net.timewalker.ffmq4.utils.watchdog.ActivityWatchdog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TcpPacketReceiver
 */
public class TcpPacketReceiver extends AbstractTcpPacketHandler implements Runnable
{
    private static final Log log = LogFactory.getLog(TcpPacketReceiver.class);
    
    // Attributes
    private TcpPacketTransport transport;
    private NetworkInputChannel inChannel;
    private int pingInterval;
    private int maxPacketSize;
    
    // Runtime
    private boolean traceEnabled;
    private boolean stopRequired = false;
    private boolean trustedConnection = false;
    
    /**
     * Constructor
     */
    protected TcpPacketReceiver( TcpPacketTransport transport , NetworkInputChannel inputChannel , PacketTransportListener listener , int pingInterval , int maxPacketSize )
    {
        super(transport.getId(),listener);
        this.transport = transport;
        this.pingInterval = pingInterval;
        this.inChannel = inputChannel;
        this.maxPacketSize = maxPacketSize;
        this.traceEnabled = log.isTraceEnabled();
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
	public void run()
    {
        try
        {
        	if (pingInterval > 0)
        		ActivityWatchdog.getInstance().register(this);
        	
            while (!stopRequired)
            {
            	int actualMaxPacketSize = Integer.MAX_VALUE;
            	if (maxPacketSize != -1)
            		actualMaxPacketSize = trustedConnection ? maxPacketSize : 1024;

                AbstractPacket packet = receive(actualMaxPacketSize);
                if (packet == null)
                {
                	if (stopRequired)
                		break;
                	
                	// Report connection closed
                    log.debug("#"+id+" connection closed by remote peer.");
                    transport.closeTransport(true);
                    break;
                }
                
                if (traceEnabled)
                    log.trace("#"+id+" Received "+packet);
                
                lastActivity = System.currentTimeMillis();
                if (listener != null)
                	trustedConnection = listener.packetReceived(packet);
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
    	return pingInterval*1000L*2;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.watchdog.ActiveObject#onActivityTimeout()
     */
    @Override
	public boolean onActivityTimeout() throws Exception
    {
    	log.warn("#"+id+" ping timeout on client socket, closing connection.");
    	transport.closeTransport(true);
       	return true;
    }
    
    public void pleaseStop()
    {
        stopRequired = true;
        if (pingInterval > 0)
        	ActivityWatchdog.getInstance().unregister(this);
    }
    
    public AbstractPacket receive( int maxPacketSize ) throws PacketTransportException
    {
        RawDataBuffer buffer = inChannel.ioBuffer;
        buffer.clear();
        
        // Receive data
        int responseSize = receiveSize(inChannel.stream);
        if (responseSize == -1)
            return null;
        
        // Security check
        if (responseSize > maxPacketSize)
            throw new PacketTransportException("Packet is too large : "+responseSize+" (maxPacketSize="+maxPacketSize+"), dropping client.");
        
        if (!receiveData(inChannel.ioBuffer, inChannel.stream, responseSize))
            return null;
        
        // Unserialize response
        buffer.reset();
        try
        {
            return PacketSerializer.unserializeFrom(buffer);
        }
        catch (Exception e)
        {
            log.error("Cannot unserialize packet",e);
            return null;
        }
    }
    
    private int receiveSize( InputStream in )
    {
        try
        {
            int ch1 = in.read();
            int ch2 = in.read();
            int ch3 = in.read();
            int ch4 = in.read();
            if ((ch1 | ch2 | ch3 | ch4) < 0)
                return -1;
            
            return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
        }
        catch (IOException e)
        {
            return -1;
        }
    }
    
    private boolean receiveData( RawDataBuffer buffer , InputStream in , int amount ) throws PacketTransportException
    {
        int received = 0;
        try
        {
            buffer.ensureCapacity(amount);
            while (received < amount)
            {
                int readAmount = buffer.readFrom(in, received, amount - received);
                if (readAmount <= 0)
                    return false;
                
                received += readAmount;
            }
            return true;
        }
        catch (IOException e)
        {
            throw new PacketTransportException("Connection read error",e);
        }
    }
}
