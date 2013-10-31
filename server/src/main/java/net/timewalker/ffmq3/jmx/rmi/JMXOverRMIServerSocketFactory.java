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
package net.timewalker.ffmq3.jmx.rmi;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.net.ServerSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JMXRMIServerSocketFactory
 */
public final class JMXOverRMIServerSocketFactory implements RMIServerSocketFactory
{
	private static final Log log = LogFactory.getLog(JMXOverRMIServerSocketFactory.class);
	
	// Attributes
    private String listenAddr;
    private int backLog;
    private boolean manageSockets;
    
    // Cache
    private InetAddress listenIf;
    private ServerSocketFactory socketFactory;
    
    // Runtime
    private List<ServerSocket> createdSockets = new Vector<>();
    
    /**
     * Constructor
     */
    public JMXOverRMIServerSocketFactory( int backLog , String listenAddr , boolean manageSockets )
    {
        this.backLog = backLog;
        this.listenAddr = listenAddr;
        this.manageSockets = manageSockets;
    }
   
    private synchronized ServerSocketFactory getSocketFactory()
    {
        if (socketFactory == null)
        	socketFactory = ServerSocketFactory.getDefault();
        
        return socketFactory;
    }
    
    private synchronized InetAddress getListenAddress() throws UnknownHostException
    {
        if (listenIf == null)
        {
            // Resolve listen interface
            listenIf = InetAddress.getByName(listenAddr);
        }
        return listenIf; 
    }
    
    /*
     * (non-Javadoc)
     * @see java.rmi.server.RMIServerSocketFactory#createServerSocket(int)
     */
    @Override
	public ServerSocket createServerSocket(int port) throws IOException
    {
    	ServerSocket socket = getSocketFactory().createServerSocket(port,backLog,getListenAddress());
    	if (manageSockets)
    		createdSockets.add(socket);
    	return socket;
    }
    
    /**
     * Cleanup sockets created by this factory
     */
    public void close()
    {
    	if (!manageSockets)
    		throw new IllegalStateException("Cannot close an un-managed socket factory");
    	
    	synchronized (createdSockets)
		{
	    	Iterator<ServerSocket> sockets = createdSockets.iterator();
	    	while (sockets.hasNext())
	    	{
	    		ServerSocket socket = sockets.next();
	    		try
	    		{
	    			socket.close();
	    		}
	    		catch (Exception e)
	    		{
	    			log.error("Could not close server socket",e);
	    		}
	    	}
	    	createdSockets.clear();
		}
    }
}
