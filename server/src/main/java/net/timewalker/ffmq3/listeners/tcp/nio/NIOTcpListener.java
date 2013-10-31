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
package net.timewalker.ffmq3.listeners.tcp.nio;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import javax.jms.JMSException;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.FFMQServerSettings;
import net.timewalker.ffmq3.jmx.JMXAgent;
import net.timewalker.ffmq3.listeners.ClientProcessor;
import net.timewalker.ffmq3.listeners.tcp.AbstractTcpClientListener;
import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.transport.PacketTransportException;
import net.timewalker.ffmq3.transport.PacketTransportType;
import net.timewalker.ffmq3.transport.tcp.nio.NIOClientSocketHandler;
import net.timewalker.ffmq3.transport.tcp.nio.NIOServerSocketHandler;
import net.timewalker.ffmq3.transport.tcp.nio.NIOTcpMultiplexer;
import net.timewalker.ffmq3.transport.tcp.nio.NIOTcpPacketTransport;
import net.timewalker.ffmq3.utils.Settings;
import net.timewalker.ffmq3.utils.id.UUIDProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * NIOTcpListener
 */
public final class NIOTcpListener extends AbstractTcpClientListener implements NIOServerSocketHandler, NIOTcpListenerMBean
{
	private static final Log log = LogFactory.getLog(NIOTcpListener.class);
	
	// Runtime
	private ServerSocketChannel serverSocketChannel;
	private NIOTcpMultiplexer multiplexer;
	
	/**
	 * Constructor
	 */
	public NIOTcpListener( FFMQEngine engine ,
				           String listenAddr , 
				           int listenPort ,
				           Settings settings )
	{
		this(engine,listenAddr,listenPort,settings,null);
	}
	
	/**
     * Constructor
     */
    public NIOTcpListener( FFMQEngine engine ,
                           String listenAddr , 
                           int listenPort ,
                           Settings settings ,
                           JMXAgent jmxAgent )
    {
        super(engine,settings,jmxAgent,listenAddr,listenPort);
    }

	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ClientListener#getName()
	 */
	public String getName()
	{
		return PacketTransportType.TCPNIO+"-"+listenAddr+"-"+listenPort;
	}
	
	private void initServerSocket() throws JMSException
	{
		try
		{
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().setReuseAddress(true);
			
			int tcpBackLog = settings.getIntProperty(FFMQServerSettings.LISTENER_TCP_BACK_LOG, DEFAULT_TCP_BACK_LOG);

            InetAddress bindAddress = getBindAddress();
			InetSocketAddress isa = new InetSocketAddress(bindAddress, listenPort);
			serverSocketChannel.socket().bind(isa,tcpBackLog);
		}
		catch (Exception e)
        {
        	throw new FFMQException("Could not configure server socket","NETWORK_ERROR",e);
        }
	}
		
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.AbstractListener#start()
	 */
	public synchronized void start() throws JMSException
	{
		if (started)
			return;
		
		log.info(" Starting listener ["+getName()+"]");
		
		initServerSocket();
		
		multiplexer = new NIOTcpMultiplexer(settings,false);
		multiplexer.registerServerSocketHandler(this);
		
		started = true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.AbstractListener#stop()
	 */
	public void stop()
	{
		if (!started)
			return;
		
		log.info("Stopping listener ["+getName()+"]");
		
		// Close the listen socket
		multiplexer.unregisterServerSocketHandler(this);
		multiplexer.stop();
		multiplexer = null;
		
		// Close remaining clients
		closeRemainingClients();
		
		started = false;
	}

	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.tcp.nio.NIOServerSocketHandler#createClientHandler(net.timewalker.ffmq3.transport.tcp.nio.NIOTcpMultiplexer, java.nio.channels.SocketChannel)
	 */
	public NIOClientSocketHandler createClientHandler(NIOTcpMultiplexer multiplexer, SocketChannel socketChannel)
	{
		try
		{
		    String clientId = UUIDProvider.getInstance().getShortUUID();
			NIOTcpPacketTransport transport = new NIOTcpPacketTransport(clientId,multiplexer,socketChannel,settings);
			ClientProcessor clientProcessor = new ClientProcessor(clientId,this,localEngine,transport);
			registerClient(clientProcessor);
			clientProcessor.start();
			
			return transport;
		}
		catch (PacketTransportException e)
		{
			log.error("Cannot create client processor",e);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.transport.tcp.nio.NIOServerSocketHandler#getServerSocketChannel()
	 */
	public ServerSocketChannel getServerSocketChannel()
	{
		return serverSocketChannel;
	}
}
