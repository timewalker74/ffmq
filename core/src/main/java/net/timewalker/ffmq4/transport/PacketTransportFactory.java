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
package net.timewalker.ffmq3.transport;

import java.net.URI;

import net.timewalker.ffmq3.client.ClientEnvironment;
import net.timewalker.ffmq3.transport.tcp.io.TcpPacketTransport;
import net.timewalker.ffmq3.transport.tcp.nio.NIOTcpPacketTransport;
import net.timewalker.ffmq3.utils.Settings;

/**
 * PacketTransportFactory
 */
public class PacketTransportFactory
{
    private static PacketTransportFactory instance = null;
    
    /**
     * Get the singleton instance
     */
    public static synchronized PacketTransportFactory getInstance()
    {
        if (instance == null)
            instance = new PacketTransportFactory();
        return instance;
    }
    
    //----------------------------------------------------------------------------
    
    /**
     * Constructor (private)
     */
    private PacketTransportFactory()
    {
        // Nothing
    }
    
    /**
     * Create a packet transport instance to handle the given URI
     */
    public PacketTransport createPacketTransport( String id , URI transportURI , Settings settings ) throws PacketTransportException
    {
        String protocol = transportURI.getScheme();
        if (protocol == null)
            protocol = PacketTransportType.TCP; // Default protocol
        
        if (protocol.equals(PacketTransportType.TCP) ||
            protocol.equals(PacketTransportType.TCPS))
        {
            return new TcpPacketTransport(id,transportURI,settings);
        }
        	
        if (protocol.equals(PacketTransportType.TCPNIO))
        {
        	return new NIOTcpPacketTransport(id,ClientEnvironment.getMultiplexer(),transportURI,settings);
        }
        
        throw new PacketTransportException("Unsupported transport protocol : "+protocol);
    }
}
