package net.timewalker.ffmq3.transport;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;

import net.timewalker.ffmq3.FFMQClientSettings;
import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.client.ClientEnvironment;
import net.timewalker.ffmq3.transport.packet.AbstractQueryPacket;
import net.timewalker.ffmq3.transport.packet.AbstractResponsePacket;
import net.timewalker.ffmq3.transport.packet.response.ErrorResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PacketTransportEndpoint
 */
public final class PacketTransportEndpoint
{
    protected static final Log log = LogFactory.getLog(PacketTransportEndpoint.class);
    
    // Attributes
    private int id;
    private String fullId;
    private PacketTransportHub parentHub;
    private int transportTimeout;
    
    // Runtime
    private Semaphore responseSemaphore = new Semaphore(0);
    private AbstractResponsePacket response;
    private boolean traceEnabled;

    /**
     * Constructor
     */
    public PacketTransportEndpoint( int id , PacketTransportHub parentHub )
    {
        this.id = id;
        this.parentHub = parentHub;
        this.fullId =  parentHub.getTransport().getId()+"-"+id;
        this.traceEnabled = log.isTraceEnabled();
        this.transportTimeout = ClientEnvironment.getSettings().getIntProperty(FFMQClientSettings.TRANSPORT_TIMEOUT,30);
    }
    
    public int getId()
    {
        return id;
    }
    
    protected Semaphore getResponseSemaphore()
    {
        return responseSemaphore;
    }
    
    protected void setResponse(AbstractResponsePacket response)
    {
        this.response = response;
    }

    public synchronized AbstractResponsePacket blockingRequest( AbstractQueryPacket query ) throws JMSException
    {
    	response = null; // Make sure response is clear before proceeding
        query.setEndpointId(id);
        
        if (traceEnabled)
            log.trace("["+fullId+"] blockingRequest() : Sending "+query);

        PacketTransport transport = parentHub.getTransport();
        try
        {
            transport.send(query);
        }
        catch (PacketTransportException e)
        {
            throw new FFMQException("["+fullId+"] Could not send packet on transport : "+e.toString(),"TRANSPORT_ERROR");
        }
    
        int n;
        for(n=0;n<transportTimeout;n++)
        {
        	try
        	{
	            if (responseSemaphore.tryAcquire(1,TimeUnit.SECONDS) || transport.isClosed())
	                break;
        	}
        	catch (InterruptedException e)
        	{
        		break; // Abort
        	}
        }
        
        AbstractResponsePacket receivedResponse = response;
        response = null; // Clear reference
        
        if (n == transportTimeout)
            throw new FFMQException("["+fullId+"] Timeout waiting for server response ("+transportTimeout+"s)","TRANSPORT_ERROR");
        
        if (receivedResponse == null)
            throw new FFMQException("["+fullId+"] Could not get an answer from server (Transport was closed after "+n+"s)","TRANSPORT_ERROR");
                
        if (receivedResponse instanceof ErrorResponse)
            ((ErrorResponse)receivedResponse).respawnError();

        if (traceEnabled)
            log.trace("["+fullId+"] blockingRequest() : Received "+receivedResponse);
        
        return receivedResponse;
    }
    
    public void nonBlockingRequest( AbstractQueryPacket query ) throws JMSException
    {
    	// If the send queue is too big, throttle things down by using a blocking request
    	if (parentHub.getTransport().needsThrottling())
    	{
    		if (traceEnabled)
    			log.trace("Send queue is too big, throttling down ...");
    		blockingRequest(query);
    		return;
    	}
    	
        query.setEndpointId(-1);
        
        if (traceEnabled)
            log.trace("["+fullId+"] nonBlockingRequest() : Sending "+query);

        try
        {
            parentHub.getTransport().send(query);
        }
        catch (PacketTransportException e)
        {
            throw new FFMQException("["+fullId+"] Could not send packet on transport : "+e.toString(),"TRANSPORT_ERROR");
        }
    }
    
    public void close()
    {
        parentHub.unregisterEndpoint(this);
    }
}
