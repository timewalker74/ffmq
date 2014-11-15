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
package net.timewalker.ffmq4.remote.connection;

import java.net.URI;

import javax.jms.JMSException;
import javax.jms.Session;

import net.timewalker.ffmq4.FFMQConstants;
import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.client.ClientEnvironment;
import net.timewalker.ffmq4.common.connection.AbstractConnection;
import net.timewalker.ffmq4.common.message.AbstractMessage;
import net.timewalker.ffmq4.common.session.AbstractSession;
import net.timewalker.ffmq4.remote.session.RemoteMessageConsumer;
import net.timewalker.ffmq4.remote.session.RemoteSession;
import net.timewalker.ffmq4.transport.PacketTransport;
import net.timewalker.ffmq4.transport.PacketTransportEndpoint;
import net.timewalker.ffmq4.transport.PacketTransportException;
import net.timewalker.ffmq4.transport.PacketTransportFactory;
import net.timewalker.ffmq4.transport.PacketTransportHub;
import net.timewalker.ffmq4.transport.PacketTransportListener;
import net.timewalker.ffmq4.transport.packet.AbstractPacket;
import net.timewalker.ffmq4.transport.packet.AbstractResponsePacket;
import net.timewalker.ffmq4.transport.packet.NotificationPacket;
import net.timewalker.ffmq4.transport.packet.PacketType;
import net.timewalker.ffmq4.transport.packet.query.DeleteTemporaryQueueQuery;
import net.timewalker.ffmq4.transport.packet.query.DeleteTemporaryTopicQuery;
import net.timewalker.ffmq4.transport.packet.query.OpenConnectionQuery;
import net.timewalker.ffmq4.transport.packet.query.RollbackMessageQuery;
import net.timewalker.ffmq4.transport.packet.query.SetClientIDQuery;
import net.timewalker.ffmq4.transport.packet.query.StartConnectionQuery;
import net.timewalker.ffmq4.transport.packet.query.StopConnectionQuery;
import net.timewalker.ffmq4.transport.packet.response.OpenConnectionResponse;
import net.timewalker.ffmq4.utils.ErrorTools;
import net.timewalker.ffmq4.utils.async.AsyncTask;
import net.timewalker.ffmq4.utils.id.IntegerID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * RemoteConnection
 */
public class RemoteConnection extends AbstractConnection implements PacketTransportListener
{
    protected static final Log log = LogFactory.getLog(RemoteConnection.class);
	
    // Attributes
    private URI transportURI;
    
    // Runtime
    private PacketTransport transport;
    protected PacketTransportHub transportHub;
    protected PacketTransportEndpoint transportEndpoint;
    
    /**
     * Constructor
     */
    public RemoteConnection( URI transportURI , String userName, String password , String clientID ) throws JMSException
    {
        super(clientID);
        this.transportURI = transportURI;

        try
        {
            connect(userName,password);
        }
        catch (JMSException e)
        {
        	close();
            throw e;
        }
    }
    
    private void connect( String userName, String password ) throws JMSException
    {
    	log.debug("#"+id+" Creating transport");
        try
        {
            this.transport = PacketTransportFactory.getInstance().createPacketTransport(id,transportURI,ClientEnvironment.getSettings());
            this.transport.setListener(this);
            this.transport.start();
        }
        catch (PacketTransportException e)
        {
            throw new FFMQException("Could not establish transport to "+transportURI,"TRANSPORT_ERROR",e);
        }
        
        this.transportHub = new PacketTransportHub(transport);
        this.transportEndpoint = transportHub.createEndpoint();
        
        log.debug("#"+id+" Opening connection context");
        OpenConnectionQuery query = new OpenConnectionQuery();        
        query.setUserName(userName);
        query.setPassword(password);
        query.setClientID(clientID);
        OpenConnectionResponse response = (OpenConnectionResponse)transportEndpoint.blockingRequest(query);
        
        // Check protocol version
        if (response.getProtocolVersion() != FFMQConstants.TRANSPORT_PROTOCOL_VERSION)
            throw new FFMQException("Transport protocol version mismatch (client is "+FFMQConstants.TRANSPORT_PROTOCOL_VERSION+", server is "+response.getProtocolVersion()+")","PROTOCOL_MISMATCH");
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.common.connection.AbstractConnection#setClientID(java.lang.String)
     */
    @Override
	public final void setClientID(String clientID) throws JMSException
    {
        super.setClientID(clientID);
        
        SetClientIDQuery query = new SetClientIDQuery();
        query.setClientID(clientID);
        transportEndpoint.blockingRequest(query);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.common.connection.AbstractConnection#onConnectionClose()
     */
    @Override
	protected void onConnectionClose()
    {
    	super.onConnectionClose();
    	
    	try
        {
        	if (transport != null && !transport.isClosed())
        	{
	            log.debug("#"+id+" Closing transport");
	            this.transport.close();
        	}
        }
        catch (Exception e)
        {
            log.error("Could not close transport",e);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.common.connection.AbstractConnection#onConnectionClosed()
     */
    @Override
	protected void onConnectionClosed()
    {
        super.onConnectionClosed();
        if (transportHub != null)
            transportHub.close();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.common.connection.AbstractConnection#deleteTemporaryQueue(java.lang.String)
     */
    @Override
	public final void deleteTemporaryQueue(String queueName) throws JMSException
    {
        DeleteTemporaryQueueQuery query = new DeleteTemporaryQueueQuery();
        query.setQueueName(queueName);
        transportEndpoint.blockingRequest(query);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.common.connection.AbstractConnection#deleteTemporaryTopic(java.lang.String)
     */
    @Override
	public final void deleteTemporaryTopic(String topicName) throws JMSException
    {
        DeleteTemporaryTopicQuery query = new DeleteTemporaryTopicQuery();
        query.setTopicName(topicName);
        transportEndpoint.blockingRequest(query);
    }

    /* (non-Javadoc)
     * @see javax.jms.Connection#createSession(boolean, int)
     */
    @Override
	public final Session createSession(boolean transacted, int acknowledgeMode) throws JMSException
    {
        if (!transacted && acknowledgeMode == Session.SESSION_TRANSACTED)
            throw new FFMQException("Acknowledge mode SESSION_TRANSACTED cannot be used for an non-transacted session","INVALID_ACK_MODE");
        
        externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        RemoteSession session = new RemoteSession(idProvider.createID(),
	        		                                  this,
	                                                  transportHub.createEndpoint(),
	                                                  transacted,
	                                                  acknowledgeMode);
	        registerSession(session);
	        session.remoteInit();
	        return session;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    }

    /* (non-Javadoc)
     * @see javax.jms.Connection#start()
     */
    @Override
	public final void start() throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
	        checkNotClosed();
	        if (started)
	            return;
	        started = true;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    	transportEndpoint.blockingRequest(new StartConnectionQuery());
    }

    /* (non-Javadoc)
     * @see javax.jms.Connection#stop()
     */
    @Override
	public final void stop() throws JMSException
    {
    	externalAccessLock.readLock().lock();
    	try
		{
    		checkNotClosed();
    		if (!started)
    			return;
    		started = false;
		}
    	finally
    	{
    		externalAccessLock.readLock().unlock();
    	}
    	transportEndpoint.blockingRequest(new StopConnectionQuery());
    	waitForDeliverySync();
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.remote.transport.PacketTransportListener#packetReceived(net.timewalker.ffmq4.remote.transport.packet.AbstractPacket)
     */
    @Override
	public final boolean packetReceived(AbstractPacket packet)
    {
        if (packet.getType() == PacketType.NOTIFICATION)
        {
            final NotificationPacket notifPacket = (NotificationPacket)packet;
            final AbstractMessage prefetchedMessage = notifPacket.getMessage();
            
            boolean acceptedByConsumer = false;
                
    		AbstractSession session = lookupRegisteredSession(notifPacket.getSessionId());
            if (session != null)
            {                   
                RemoteMessageConsumer consumer = (RemoteMessageConsumer)session.lookupRegisteredConsumer(notifPacket.getConsumerId());
                if (consumer != null)		
                    acceptedByConsumer = consumer.addToPrefetchQueue(prefetchedMessage,notifPacket.isDonePrefetching());
                else
                	log.debug("#"+id+" No such consumer : #"+notifPacket.getSessionId()+":"+notifPacket.getConsumerId()); // Consumer was concurrently closed or not yet registered
            }
            else
            	log.debug("#"+id+" No such session : #"+notifPacket.getSessionId()); // Session was concurrently closed
            
            // If the consumer was already gone, we need to rollback the prefetched message on the server side
            if (!acceptedByConsumer)
                scheduleRollback(notifPacket.getSessionId(),
                		         notifPacket.getConsumerId(),
                		         prefetchedMessage);
            
            return true;
        }
        
        if (packet.getType() == PacketType.R_PING)
        	return true;
        
        // Standard response, route to target endpoint
        transportHub.routeResponse((AbstractResponsePacket)packet);
        
        return true;
    }

    private void scheduleRollback( final IntegerID sessionId , final IntegerID consumerId , AbstractMessage message )
    {
        try
        {
        	final String messageId = message.getJMSMessageID();
            
            // Schedule an async rollback
            ClientEnvironment.getAsyncTaskManager().execute(new AsyncTask()
            {
                /*
                 * (non-Javadoc)
                 * @see net.timewalker.ffmq4.utils.async.AsyncTask#isMergeable()
                 */
                @Override
				public boolean isMergeable() { return false; }
                
                /*
                 * (non-Javadoc)
                 * @see net.timewalker.ffmq4.utils.async.AsyncTask#execute()
                 */
                @Override
				public void execute()
                {
                    RollbackMessageQuery query = new RollbackMessageQuery();
                    query.setSessionId(sessionId);
                    query.setConsumerId(consumerId);
                    query.setMessageId(messageId);
                    try
                    {
                        transportEndpoint.blockingRequest(query);
                    }
                    catch (JMSException e)
                    {
                        ErrorTools.log(e, log);
                    }
                }
            });
        }
        catch (JMSException e)
        {
            ErrorTools.log(e, log);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.remote.transport.PacketTransportListener#packetSent(net.timewalker.ffmq4.remote.transport.packet.AbstractPacket)
     */
    @Override
	public final void packetSent(AbstractPacket packet)
    {
        // Nothing
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.remote.transport.PacketTransportListener#transportClosed(boolean)
     */
    @Override
    public final void transportClosed(boolean linkFailed,boolean mayBlock)
    {
        if (linkFailed)
        {
        	close();
        	
        	if (mayBlock)
        		exceptionOccured(new FFMQException("Server connection lost","NETWORK_FAILURE"));
        	else
        	{
        		try
        		{
	        		ClientEnvironment.getAsyncTaskManager().execute(new AsyncTask() {
	        			/*
	        			 * (non-Javadoc)
	        			 * @see net.timewalker.ffmq3.utils.async.AsyncTask#isMergeable()
	        			 */
	        			@Override
						public boolean isMergeable() 
						{
							return false;
						}
						
						/*
						 * (non-Javadoc)
						 * @see net.timewalker.ffmq3.utils.async.AsyncTask#execute()
						 */
						@Override
						public void execute() 
						{
							exceptionOccured(new FFMQException("Server connection lost","NETWORK_FAILURE"));
						}
					});
        		}
        		catch (JMSException e)
                {
        			ErrorTools.log(e, log);
                }
        	}
        }
    }
}
