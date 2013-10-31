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
package net.timewalker.ffmq4;

/**
 * FFMQServerSettings
 */
public final class FFMQServerSettings
{
	// General listener
	public static final String LISTENER_AUTH_TIMEOUT               = "listener.auth.timeout";
	
    // TCP Listener
    public static final String LISTENER_TCP_ENABLED                = "listener.tcp.enabled";
    public static final String LISTENER_TCP_BACK_LOG               = "listener.tcp.backLog";
    public static final String LISTENER_TCP_LISTEN_ADDR            = "listener.tcp.listenAddr";
    public static final String LISTENER_TCP_LISTEN_PORT            = "listener.tcp.listenPort";
    public static final String LISTENER_TCP_USE_NIO                = "listener.tcp.useNIO";
    public static final String LISTENER_TCP_CAPACITY               = "listener.tcp.capacity";
    
    // Remote administration
    public static final String REMOTE_ADMIN_ENABLED                = "management.remoteAdmin.enabled";
    
    // Security
    public static final String SECURITY_CONNECTOR_XML_SECURITY     = "security.connector.xml.securityFile";
}
