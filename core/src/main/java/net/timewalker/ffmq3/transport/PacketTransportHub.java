package net.timewalker.ffmq3.transport;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.transport.packet.AbstractResponsePacket;

/**
 * PacketTransportHub
 */
public final class PacketTransportHub
{
    // Attributes
    private PacketTransport transport;
    
    // Runtime
    private Map registeredEndpoints = new Hashtable();
    private boolean closed;
    private int nextEndpointId;
    
    /**
     * Constructor
     */
    public PacketTransportHub( PacketTransport transport )
    {
        this.transport = transport;
    }
    
    public PacketTransport getTransport()
    {
        return transport;
    }
    
    /**
     * Create a new transport endpoint
     * @return a new transport endpoint
     */
    public synchronized PacketTransportEndpoint createEndpoint() throws JMSException
    {
        if (closed || transport.isClosed())
            throw new FFMQException("Transport is closed", "TRANSPORT_CLOSED");
        
        PacketTransportEndpoint endpoint = new PacketTransportEndpoint(nextEndpointId++, this);
        registeredEndpoints.put(new Integer(endpoint.getId()), endpoint);
        return endpoint;
    }

    protected void unregisterEndpoint( PacketTransportEndpoint endpoint )
    {
        registeredEndpoints.remove(new Integer(endpoint.getId()));
    }
    
    public void routeResponse( AbstractResponsePacket response )
    {
        int endpointId = response.getEndpointId();
        if (endpointId == -1)
        	return; // Response to an async call
        
        PacketTransportEndpoint endpoint = (PacketTransportEndpoint)registeredEndpoints.get(new Integer(endpointId));
        if (endpoint == null)
            return; // Endpoint is gone
        
        endpoint.setResponse(response);
        endpoint.getResponseSemaphore().release();
    }
    
    public synchronized void close()
    {
        if (closed)
            return;
        closed = true;
        
        synchronized (registeredEndpoints)
        {
            Iterator endpoints = registeredEndpoints.values().iterator();
            while (endpoints.hasNext())
            {
                PacketTransportEndpoint endpoint = (PacketTransportEndpoint)endpoints.next();
                endpoint.getResponseSemaphore().release();
            }
            registeredEndpoints.clear();
        }
    }
}
