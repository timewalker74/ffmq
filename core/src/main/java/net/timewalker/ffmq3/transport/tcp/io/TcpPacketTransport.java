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

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;

import javax.jms.JMSException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import net.timewalker.ffmq3.FFMQClientSettings;
import net.timewalker.ffmq3.FFMQCoreSettings;
import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.transport.PacketTransportException;
import net.timewalker.ffmq3.transport.PacketTransportType;
import net.timewalker.ffmq3.transport.packet.AbstractPacket;
import net.timewalker.ffmq3.transport.tcp.AbstractTcpPacketTransport;
import net.timewalker.ffmq3.transport.tcp.SocketUtils;
import net.timewalker.ffmq3.utils.Settings;
import net.timewalker.ffmq3.utils.ssl.PermissiveTrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TcpPacketTransport
 */
public final class TcpPacketTransport extends AbstractTcpPacketTransport
{
    private static final Log log = LogFactory.getLog(TcpPacketTransport.class);

    // Attributes
    private Settings settings;
    private int maxPacketSize;
    
    // Runtime
    private Socket socket;    
    private TcpPacketReceiver receiver;
    private TcpPacketSender sender;
    private Thread receiverThread;
    private Thread senderThread;
    
    /**
     * Constructor
     */
    public TcpPacketTransport( String id , URI transportURI , Settings settings ) throws PacketTransportException
    {
        super(id,true,settings);
        init(settings);
        this.socket = connect(transportURI);
    }
    
    /**
     * Constructor
     */
    public TcpPacketTransport( String id ,  Socket socket , Settings settings ) throws PacketTransportException
    {
        super(id,false,settings);
        this.socket = SocketUtils.setupSocket(socket,
                                              socketSendBufferSize,
                                              socketRecvBufferSize);
        init(settings);
    }
    
    private void init( Settings settings )
    {
    	this.settings = settings;
    	this.maxPacketSize = settings.getIntProperty(FFMQCoreSettings.TRANSPORT_TCP_PACKET_MAX_SIZE, 1024*1024+1024);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.transport.tcp.AbstractTcpPacketTransport#getRemotePeer()
     */
    public SocketAddress getRemotePeer()
    {
    	return socket.getRemoteSocketAddress();
    }
    
    /**
     * Connect the transport to its remote endpoint
     */
    private Socket connect( URI transportURI ) throws PacketTransportException
    {
        String protocol = transportURI.getScheme();
        String host = transportURI.getHost();
        int port = transportURI.getPort();
        int connectTimeout = settings.getIntProperty(FFMQClientSettings.TRANSPORT_TCP_CONNECT_TIMEOUT, 30);
        
        try
        {
            Socket socket = SocketUtils.setupSocket(createSocket(protocol),
	                                                socketSendBufferSize,
	                                                socketRecvBufferSize); 
            
            log.debug("#"+id+" opening a TCP connection to "+host+":"+port);
            socket.connect(new InetSocketAddress(host,port),connectTimeout*1000);
            
            return socket;
        }
        catch (ConnectException e)
        {
        	log.error("#"+id+" could not connect to "+host+":"+port+" (timeout="+connectTimeout+"s) : "+e.getMessage());
            throw new PacketTransportException("Could not connect to "+host+":"+port+" : "+e.toString());
        }
        catch (Exception e)
        {
            log.error("#"+id+" could not connect to "+host+":"+port+" (timeout="+connectTimeout+"s)",e);
            throw new PacketTransportException("Could not connect to "+host+":"+port+" : "+e.toString());
        }
    }
    
    private Socket createSocket( String protocol ) throws JMSException
    {
        if (protocol.equals(PacketTransportType.TCPS))
        {
            try
            {
                return createSSLContext().getSocketFactory().createSocket();
            }
            catch (IOException e)
            {
                throw new FFMQException("Cannot create SSL socket","TRANSPORT_ERROR",e);
            }
        }
        else
            return new Socket();
    }
    
    private SSLContext createSSLContext() throws JMSException
    {
        try
        {
            String sslProtocol = settings.getStringProperty(FFMQClientSettings.TRANSPORT_TCP_SSL_PROTOCOL, "SSLv3");
            boolean ignoreCertificates = settings.getBooleanProperty(FFMQClientSettings.TRANSPORT_TCP_SSL_IGNORE_CERTS, false);
            
            SSLContext sslContext = SSLContext.getInstance(sslProtocol);
            log.debug("#"+id+" created an SSL context : protocol=["+sslContext.getProtocol()+"] provider=["+sslContext.getProvider()+"]");
            
            // Load available keys
            KeyManager[] keyManagers = null;
            TrustManager[] trustManagers = null;

            if (ignoreCertificates)
                trustManagers = new TrustManager[] { new PermissiveTrustManager() };
            
            sslContext.init(keyManagers,trustManagers, null);
            
            return sslContext;
        }
        catch (Exception e)
        {
            throw new FFMQException("Cannot create SSL context","TRANSPORT_ERROR",e);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.remote.transport.PacketTransport#start()
     */
    public void start() throws PacketTransportException
    {
        try
        {
        	NetworkInputChannel inputChannel = 
        		new NetworkInputChannel(initialPacketBufferSize,
        				                new TcpBufferedInputStream(socket.getInputStream(),streamRecvBufferSize));
        	NetworkOutputChannel outputChannel = 
        		new NetworkOutputChannel(initialPacketBufferSize,
        				                 new TcpBufferedOutputStream(socket.getOutputStream(),streamSendBufferSize));
        	
            sender = new TcpPacketSender(this,
            		                     outputChannel,
            		                     listener,
            		                     client ? pingInterval : -1,
            		                     sendQueueMaxSize);
            
            receiver = new TcpPacketReceiver(this,
            								 inputChannel,
            		                         listener,
            		                         pingInterval,
            		                         client ? -1 : maxPacketSize);
            
            senderThread = new Thread(sender,"TcpPacketSender["+(client ? "client" : "server")+"]");
            senderThread.start();
            
            receiverThread = new Thread(receiver,"TcpPacketReceiver["+(client ? "client" : "server")+"]");
            receiverThread.start();
            
            System.out.println(Thread.activeCount());
        }
        catch (Exception e)
        {
            log.error("#"+id+" cannot start TCP packet I/O handlers",e);
            throw new PacketTransportException("Cannot start TCP packet I/O handlers : "+e.toString());
        }
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.remote.transport.PacketTransport#send(net.timewalker.ffmq3.remote.transport.packet.AbstractPacket)
     */
    public void send( AbstractPacket packet ) throws PacketTransportException
    {
        if (closed)
   			throw new PacketTransportException("Transport is closed");
        
        sender.send(packet);
    }
    
    public boolean needsThrottling()
    {
    	return sender.needsThrottling();
    }
    
    protected void closeTransport( boolean linkFailed )
    {
    	synchronized (closeLock)
		{
	    	if (closed)
	            return; // Already closed
	    	closed = true;
		}
    	
    	if (sender != null)
    		sender.pleaseStop();
    	
    	if (receiver != null)
    		receiver.pleaseStop();
    	
    	// Close the socket
        try
        {
        	if (socket != null)
        		socket.close();
        }
        catch (Exception e)
        {
            log.error("#"+id+" cannot close socket",e);
        }
        finally
        {
        	socket = null;
        }
    	
    	// Notify listener
		if (listener != null)
			listener.transportClosed(linkFailed);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.remote.transport.PacketTransport#close()
     */
    public void close()
    {
        closeTransport(false);
        
        // Wait for threads to complete
        try
		{
        	if (receiverThread != null && Thread.currentThread() != receiverThread)
        		receiverThread.join();
		}
		catch (InterruptedException e)
		{
			log.error("#"+id+" wait for receiver thread termination was interrupted.");
		}
		try
		{
        	if (senderThread != null && Thread.currentThread() != senderThread)
        		senderThread.join();
		}
		catch (InterruptedException e)
		{
			log.error("#"+id+" wait for sender thread termination was interrupted.");
		}
    }
}
