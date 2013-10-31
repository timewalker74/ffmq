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

package net.timewalker.ffmq3.jmx;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

/**
 * <p>
 *  Custom implementation of an {@link RMIClientSocketFactory} to be used by JMX clients operating over RMI.
 *  Allows the binding of a specified network interface.
 * </p>
 * <p>
 *  <b>IMPORTANT : If used, the connecting RMI client must have this class definition in its classpath</b>
 * </p>
 * <p> 
 *    For example if using the jconsole utility, you need to add the ffmq-core.jar to its classpath like this : 
 *    jconsole -J-Djava.class.path=JAVA_HOME/lib/jconsole.jar:JAVA_HOME/lib/tools.jar:&lt;path to ffmq-core.jar&gt;</b>
 * </p>
 * TODO option to use
 */
public final class JMXRMIClientSocketFactory implements RMIClientSocketFactory, Serializable
{
	private static final long serialVersionUID = 1L;
	
	// Attributes
	private String serverListenAddr;
	
	// Runtime
	private boolean targetSet;
	private String targetHost;
	
	/**
     * Constructor
     */
    public JMXRMIClientSocketFactory( String serverListenAddr )
    {
        this.serverListenAddr = serverListenAddr;
    }
	
    private String getTargetHost( String host ) throws IOException
    {
    	if (!targetSet)
    	{
    		if (serverListenAddr != null)
        	{
        		InetAddress targetInetAddr = InetAddress.getByName(serverListenAddr);
        		if (!targetInetAddr.isAnyLocalAddress())
        			targetHost = targetInetAddr.getHostAddress();
        	}
        	targetSet = true;
    	}

    	return targetHost != null ? targetHost : host;
    }
    
	/* (non-Javadoc)
	 * @see java.rmi.server.RMIClientSocketFactory#createSocket(java.lang.String, int)
	 */
	@Override
	public Socket createSocket(String host, int port) throws IOException
	{
		return new Socket(getTargetHost(host),port);
	}
}
