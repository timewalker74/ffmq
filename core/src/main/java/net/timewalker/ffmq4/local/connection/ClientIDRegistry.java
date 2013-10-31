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
package net.timewalker.ffmq4.local.connection;

import java.util.HashSet;
import java.util.Set;

import javax.jms.InvalidClientIDException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Global registry of JMS client IDs</p>
 */
public final class ClientIDRegistry
{
    private static final Log log = LogFactory.getLog(ClientIDRegistry.class);

    //-------------------------------------------------------------------------
    
    private static ClientIDRegistry instance = null;
    
    /**
     * Get the singleton instance
     */
    public static synchronized ClientIDRegistry getInstance()
    {
        if (instance == null)
            instance = new ClientIDRegistry();
        return instance;
    }
    
    //-------------------------------------------------------------------------
    
    private Set<String> clientIDs = new HashSet<>();
    
    /**
     * Constructor (Private)
     */
    private ClientIDRegistry()
    {
        super();
    }
    
    /**
     * Register a new client ID
     */
    public synchronized void register( String clientID ) throws InvalidClientIDException
    {
        if (!clientIDs.add(clientID))
        {
            log.error("Client ID already exists : "+clientID);
            throw new InvalidClientIDException("Client ID already exists : "+clientID);
        }
        log.debug("Registered clientID : "+clientID);
    }
    
    /**
     * Register a new client ID
     */
    public synchronized void unregister( String clientID )
    {
        if (clientIDs.remove(clientID))
            log.debug("Unregistered clientID : "+clientID);
    }
}
