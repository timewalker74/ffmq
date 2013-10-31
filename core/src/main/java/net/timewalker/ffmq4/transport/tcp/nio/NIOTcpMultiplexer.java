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
package net.timewalker.ffmq4.transport.tcp.nio;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.timewalker.ffmq4.FFMQCoreSettings;
import net.timewalker.ffmq4.transport.PacketTransportException;
import net.timewalker.ffmq4.transport.tcp.SocketUtils;
import net.timewalker.ffmq4.utils.Settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * NIOTcpMultiplexer
 */
public final class NIOTcpMultiplexer
{
	protected static final Log log = LogFactory.getLog(NIOTcpMultiplexer.class);

	// Attributes
	protected Selector selector;
	private SelectorThread selectorThread;
	protected int socketSendBufferSize;
	protected int socketRecvBufferSize;
	
	// Runtime
	protected List<NIOServerSocketHandler> pendingAcceptHandlers = new Vector<>();
	protected List<NIOServerSocketHandler> serverHandlers = new Vector<>();
	protected Map<String,NIOClientSocketHandler> clientHandlers = new Hashtable<>();
	private boolean waiting;
	
	/**
	 * Constructor (private)
	 */
	public NIOTcpMultiplexer( Settings settings , boolean client ) throws PacketTransportException
	{
		super();
		this.socketSendBufferSize = settings.getIntProperty(FFMQCoreSettings.TRANSPORT_TCP_SOCKET_SEND_BUFFER_SIZE,65536);	
    	this.socketRecvBufferSize = settings.getIntProperty(FFMQCoreSettings.TRANSPORT_TCP_SOCKET_RECV_BUFFER_SIZE,65536);
		try
		{
			this.selector = SelectorProvider.provider().openSelector();
			this.selectorThread = new SelectorThread(client);
			this.selectorThread.start();
		}
		catch (Exception e)
		{
			throw new PacketTransportException("Cannot create NIO multiplexer",e);
		}
	}
	
	private synchronized void wakeUpAndWait()
	{
		if (!selectorThread.isAlive())
			return;

		selector.wakeup();
		waiting = true;
		while (waiting)
		{
    		try
    		{
    			wait();
    		}
    		catch (InterruptedException e)
    		{
    			log.error("Wait was interrupted");
    			waiting = false;
    		}
		}
	}
	
	protected synchronized void onSelectExit()
	{
		if (waiting)
		{
		    waiting = false;
			notifyAll();
		}
	}
	
	/**
	 * Wake up the multiplexer so it has a chance to update read/write interests
	 */
	public void wakeUp()
	{
		selector.wakeup();
	}
	
	/**
	 * Register a new server socket handler
	 */
	public void registerServerSocketHandler( NIOServerSocketHandler serverHandler )
	{
		pendingAcceptHandlers.add(serverHandler);
		wakeUp();
	}
	
	/**
	 * Register a new client socket handler
	 */
	public void registerClientSocketHandler( NIOClientSocketHandler clientHandler )
	{
		clientHandlers.put(clientHandler.getId(), clientHandler);
		wakeUp();
	}
	
	/**
	 * Unregister a new server socket handler
	 */
	public void unregisterServerSocketHandler( NIOServerSocketHandler serverHandler )
	{
		if (pendingAcceptHandlers.remove(serverHandler))
			return; // Not handled yet
		
		if (serverHandlers.remove(serverHandler))
		{
			closeSocketChannel(serverHandler.getServerSocketChannel(), selector);
			wakeUpAndWait();
		}
	}
	
	public void unregisterClientSocketHandler( NIOClientSocketHandler clientHandler )
	{
		dropClientHandler(clientHandler,selector,false);
		wakeUp();
	}
	
	protected void dropClientHandler( NIOClientSocketHandler clientHandler , Selector selector , boolean linkFailed )
    {
		synchronized (clientHandlers)
		{
			if (clientHandlers.remove(clientHandler.getId()) != null)
				log.debug("["+clientHandler.getId()+"] Disconnecting client ("+clientHandlers.size()+" remaining)");
		}
		
        closeSocketChannel(clientHandler.getSocketChannel(),selector);
        
        if (linkFailed)
        	clientHandler.onSocketChannelClosed();
    }
	
	private void closeSocketChannel( AbstractSelectableChannel channel , Selector selector )
    {
        try
        {
            SelectionKey sk = channel.keyFor(selector);
            if (sk != null && sk.isValid())
                sk.cancel();
            if (channel.isOpen())
                channel.close();
        }
        catch (Exception e)
        {
            log.error("Could not close channel : "+e.toString());
        }
    }
	
	protected boolean acceptClient( NIOServerSocketHandler serverHandler , SocketChannel socketChannel )
    {
		synchronized (clientHandlers)
		{
			NIOClientSocketHandler clientHandler = serverHandler.createClientHandler(this,socketChannel);
			if (clientHandler == null)
				return false;

			clientHandlers.put(clientHandler.getId(), clientHandler);
			log.debug("["+clientHandler.getId()+"] Accepted new client from "+socketChannel.socket().getInetAddress().getHostAddress()+" ("+clientHandlers.size()+") : "+clientHandler.getId());
		}
        return true;
    }
	
	protected boolean readAndProcessChannelData( NIOClientSocketHandler clientHandler )
    {
        try
        {
        	ByteBuffer inputBuffer = clientHandler.getInputBuffer();
        	int readAmount = clientHandler.getSocketChannel().read(inputBuffer);
            if (readAmount <= 0)
            {
                log.debug("["+clientHandler.getId()+"] Cannot read, channel socket was closed");
                return false;
            }
            
            inputBuffer.flip();    // Prepare for reading
            boolean status = clientHandler.handleIncomingData();
            inputBuffer.compact(); // Restore pointers
            
            return status;
        }
        catch (IOException e)
        {
            log.debug("["+clientHandler.getId()+"] Read failed : "+e.getMessage());
            return false;
        }
        catch (Exception e)
        {
            log.error("["+clientHandler.getId()+"] Could not read channel data",e);
            return false;
        }
    }
	
	protected boolean writeAndProcessChannelData( NIOClientSocketHandler clientHandler )
    {
        try
        {           
            if (!clientHandler.appendOutgoingData())
                return false;
            
            ByteBuffer outputBuffer = clientHandler.getOutputBuffer();
            outputBuffer.flip(); // Prepare for reading
            int writeAmount;
            try
            {
                writeAmount = clientHandler.getSocketChannel().write(outputBuffer);
                if (writeAmount <= 0)
                    log.debug("["+clientHandler.getId()+"] Cannot write, channel socket was closed");
            }
            catch (IOException e)
            {
                log.error("["+clientHandler.getId()+"] Write failed : "+e.getMessage());
                writeAmount = -1;
            }
            outputBuffer.compact(); // Restore pointers

            return (writeAmount > 0);
        }
        catch (Exception e)
        {
            log.error("["+clientHandler.getId()+"] Could not process data",e);
            return false;
        }
    }
	
	protected void updateConnectInterest( NIOClientSocketHandler clientHandler , Selector selector )
    {		
		SocketChannel socketChannel = clientHandler.getSocketChannel();
		if (!socketChannel.isOpen())
			return;
		
        // We are interested in connect if not already done
        if (!socketChannel.isConnected())
            addInterest(socketChannel,SelectionKey.OP_CONNECT,clientHandler,selector);
        else
            removeInterest(socketChannel,SelectionKey.OP_CONNECT,selector);
    }
	
	protected void updateReadInterest( NIOClientSocketHandler clientHandler , Selector selector )
    {		
		SocketChannel socketChannel = clientHandler.getSocketChannel();
		if (!socketChannel.isOpen())
			return;
		if (!socketChannel.isConnected())
			return;
		
    	// We are interested in reading only if we have some buffer space left
        if (clientHandler.getInputBuffer().remaining() > 0)
            addInterest(socketChannel,SelectionKey.OP_READ,clientHandler,selector);
        else
            removeInterest(socketChannel,SelectionKey.OP_READ,selector);
    }
    
	protected void updateWriteInterest( NIOClientSocketHandler clientHandler , Selector selector )
    {
		SocketChannel socketChannel = clientHandler.getSocketChannel();
		if (!socketChannel.isOpen())
			return;
		if (!socketChannel.isConnected())
			return;
		
    	// We are interested in writing only if there is something in the output buffer or
    	// the handler expresses the need to write something
        if (clientHandler.getOutputBuffer().position() > 0 || clientHandler.hasWriteInterest())
            addInterest(socketChannel,SelectionKey.OP_WRITE,null,selector);
        else
            removeInterest(socketChannel,SelectionKey.OP_WRITE,selector);
    }
	
    protected void addInterest( AbstractSelectableChannel channel ,  int interest , Object attachment , Selector selector )
    {
    	try
    	{
	        SelectionKey sk = channel.keyFor(selector);
	        if (sk != null)
	        {
	        	if (!sk.isValid())
	        		return;
	        	
	            int actualInterests = sk.interestOps();
	            if ((actualInterests & interest) != interest)
	                sk.interestOps(actualInterests | interest);
	            if (attachment != null)
	                sk.attach(attachment);
	        }
	        else
	            channel.register(selector, interest, attachment);
    	}
    	catch (ClosedChannelException e)
    	{
    		log.warn("Cannot add interest to selector channel : channel is closed");
    	}
    }
    
    private void removeInterest( AbstractSelectableChannel channel ,  int interest , Selector selector )
    {
        SelectionKey sk = channel.keyFor(selector);
        if (sk != null && sk.isValid())
        {
            int actualInterests = sk.interestOps();
            if ((actualInterests & interest) != 0)
                sk.interestOps(sk.interestOps() & ~interest);
        }
    }
    
    protected boolean finalizeConnect( NIOClientSocketHandler clientHandler , SocketChannel channel , Selector selector )
    {
        try
        {
            // Finish the connection handshake
            channel.finishConnect();
            
            log.debug("["+clientHandler.getId()+"] Connected to "+channel.socket().getInetAddress());
            
            // Unregister connect interest
            removeInterest(channel, SelectionKey.OP_CONNECT, selector);
            
            return true;
        }
        catch (SocketException e)
        {
            log.error("["+clientHandler.getId()+"] Could not connect to remote server : "+e.getMessage());
            return false;
        }
        catch (Exception e)
        {
            log.error("["+clientHandler.getId()+"] Could not finalize connection",e);
            return false;
        }
    }
    
    /**
     * Stop the multiplexer
     */
    public void stop()
    {
        selectorThread.pleaseStop();
    }
    
	//-------------------------------------------------------------------------------------------------------------------
	//                                                 SELECTOR THREAD
    //-------------------------------------------------------------------------------------------------------------------
    
	private class SelectorThread extends Thread
    {
		// Runtime
		private volatile boolean stopRequired = false;
		
		/**
		 * Constructor
		 */
		public SelectorThread( boolean client )
		{
			super("NIOTcpMultiplexer-SelectorThread-"+(client ? "CLIENT" : "SERVER"));
			setPriority(MAX_PRIORITY);
			setDaemon(true);
		}
		
		public void pleaseStop()
		{
			stopRequired = true;
			selector.wakeup();
		}
		
        /*
         * (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        @Override
		public void run()
        {
            try
            {
                // Main loop
                while (!stopRequired)
                {
                    int selectedCount = selector.select();
                    if (stopRequired)
                    	break;
                    
                    onSelectExit();
                    
                    if (selectedCount > 0)
                    {
                        Set<SelectionKey> readyKeys = selector.selectedKeys();
                        Iterator<SelectionKey> i = readyKeys.iterator();
                        
                        // Walk through the active keys
                        while (i.hasNext()) 
                        {
                            SelectionKey sk = i.next();
                            i.remove();
                            
                            // Concurrently cancelled, skip
                            if (!sk.isValid())
                        		continue;
                            
                            try
                            {
	                            if (sk.isWritable())
	                            {
	                                NIOClientSocketHandler clientHandler = (NIOClientSocketHandler)sk.attachment();
	                                if (!writeAndProcessChannelData(clientHandler))
	                                {
	                                    dropClientHandler(clientHandler,selector,true);
	                                    continue;
	                                }
	                            }

	                            if (sk.isReadable())
	                            {
	                            	NIOClientSocketHandler clientHandler = (NIOClientSocketHandler)sk.attachment();
	                                if (!readAndProcessChannelData(clientHandler))
	                                {
	                                	dropClientHandler(clientHandler,selector,true);
	                                	continue;
	                                }
	                            }

	                            if (sk.isAcceptable())
	                            {
	                            	NIOServerSocketHandler serverHandler = (NIOServerSocketHandler)sk.attachment();
	                                ServerSocketChannel nextReady = (ServerSocketChannel)sk.channel();
	                                
	                                // Accept a new client socket
	                                SocketChannel clientChannel = nextReady.accept();
	                                clientChannel.configureBlocking(false);
	                                SocketUtils.setupSocket(clientChannel.socket(),
	            			                                socketSendBufferSize,
	            			                                socketRecvBufferSize);
	                                
	                                // Create a new client handler
	                                if (!acceptClient(serverHandler,clientChannel))
	                                {
	                                    log.error("Dropping incoming connection due to errors ...");
	                                    clientChannel.close();
	                                    continue;
	                                }
	                            }

	                            if (sk.isConnectable())
	                            {
	                                NIOClientSocketHandler clientHandler = (NIOClientSocketHandler)sk.attachment();
	                                if (!finalizeConnect(clientHandler,(SocketChannel)sk.channel(),selector))
	                                {
	                                    dropClientHandler(clientHandler,selector,true);
	                                    continue;
	                                }
	                            }
                            }
                            catch (CancelledKeyException e)
                            {
                            	Object attachement = sk.attachment();
                            	if (attachement instanceof NIOClientSocketHandler)
                            	{
                            		NIOClientSocketHandler clientHandler = (NIOClientSocketHandler)attachement;
                            		log.debug("["+clientHandler.getId()+"] Selection key cancelled, dropping cient ...");
                            		dropClientHandler(clientHandler,selector,true);
                            	}
                            	else
                            		log.error("Server selection key was cancelled",e);
                            }
                        }
                    }
                    
                    // Register pending server handlers 
                    synchronized (pendingAcceptHandlers)
					{
                    	if (pendingAcceptHandlers.size() > 0)
                    	{
	                    	for (int i = 0; i < pendingAcceptHandlers.size(); i++)
							{
	                    		NIOServerSocketHandler serverHandler = pendingAcceptHandlers.get(i);
	                        	addInterest(serverHandler.getServerSocketChannel(), SelectionKey.OP_ACCEPT, serverHandler, selector);
	                        	serverHandlers.add(serverHandler);
	                        }
	                    	pendingAcceptHandlers.clear();
                    	}
					}
                                       
                    // Update read/write interests
                    synchronized (clientHandlers)
                    {
                    	Iterator<NIOClientSocketHandler> clientsIt = clientHandlers.values().iterator();
                        while (clientsIt.hasNext())
                        {
                            NIOClientSocketHandler clientHandler = clientsIt.next();
                            updateConnectInterest(clientHandler,selector);
                            updateReadInterest(clientHandler,selector);
                            updateWriteInterest(clientHandler,selector);
                        }
                    }
                }
                
                selector.close();
            }
            catch (Throwable e)
            {
                log.error("Selector thread failed",e);
            }
            log.debug("Exiting");
        }
    }
}
