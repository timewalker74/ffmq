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
package net.timewalker.ffmq3.listeners;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;

import net.timewalker.ffmq3.FFMQConstants;
import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.FFMQServerSettings;
import net.timewalker.ffmq3.common.message.AbstractMessage;
import net.timewalker.ffmq3.listeners.utils.RemoteNotificationProxy;
import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.local.connection.LocalConnection;
import net.timewalker.ffmq3.local.session.LocalDurableTopicSubscriber;
import net.timewalker.ffmq3.local.session.LocalMessageConsumer;
import net.timewalker.ffmq3.local.session.LocalQueueBrowser;
import net.timewalker.ffmq3.local.session.LocalQueueBrowserEnumeration;
import net.timewalker.ffmq3.local.session.LocalSession;
import net.timewalker.ffmq3.transport.PacketTransport;
import net.timewalker.ffmq3.transport.PacketTransportException;
import net.timewalker.ffmq3.transport.PacketTransportListener;
import net.timewalker.ffmq3.transport.packet.AbstractPacket;
import net.timewalker.ffmq3.transport.packet.AbstractQueryPacket;
import net.timewalker.ffmq3.transport.packet.AbstractResponsePacket;
import net.timewalker.ffmq3.transport.packet.PacketType;
import net.timewalker.ffmq3.transport.packet.query.AbstractConsumerQuery;
import net.timewalker.ffmq3.transport.packet.query.AbstractQueueBrowserEnumerationQuery;
import net.timewalker.ffmq3.transport.packet.query.AbstractQueueBrowserQuery;
import net.timewalker.ffmq3.transport.packet.query.AbstractSessionQuery;
import net.timewalker.ffmq3.transport.packet.query.AcknowledgeQuery;
import net.timewalker.ffmq3.transport.packet.query.CloseBrowserEnumerationQuery;
import net.timewalker.ffmq3.transport.packet.query.CloseBrowserQuery;
import net.timewalker.ffmq3.transport.packet.query.CloseConsumerQuery;
import net.timewalker.ffmq3.transport.packet.query.CloseSessionQuery;
import net.timewalker.ffmq3.transport.packet.query.CommitQuery;
import net.timewalker.ffmq3.transport.packet.query.CreateBrowserQuery;
import net.timewalker.ffmq3.transport.packet.query.CreateConsumerQuery;
import net.timewalker.ffmq3.transport.packet.query.CreateDurableSubscriberQuery;
import net.timewalker.ffmq3.transport.packet.query.CreateSessionQuery;
import net.timewalker.ffmq3.transport.packet.query.CreateTemporaryQueueQuery;
import net.timewalker.ffmq3.transport.packet.query.CreateTemporaryTopicQuery;
import net.timewalker.ffmq3.transport.packet.query.DeleteTemporaryQueueQuery;
import net.timewalker.ffmq3.transport.packet.query.DeleteTemporaryTopicQuery;
import net.timewalker.ffmq3.transport.packet.query.GetQuery;
import net.timewalker.ffmq3.transport.packet.query.OpenConnectionQuery;
import net.timewalker.ffmq3.transport.packet.query.PingQuery;
import net.timewalker.ffmq3.transport.packet.query.PrefetchQuery;
import net.timewalker.ffmq3.transport.packet.query.PutQuery;
import net.timewalker.ffmq3.transport.packet.query.QueueBrowserFetchElementQuery;
import net.timewalker.ffmq3.transport.packet.query.QueueBrowserGetEnumerationQuery;
import net.timewalker.ffmq3.transport.packet.query.RecoverQuery;
import net.timewalker.ffmq3.transport.packet.query.RollbackMessageQuery;
import net.timewalker.ffmq3.transport.packet.query.RollbackQuery;
import net.timewalker.ffmq3.transport.packet.query.SetClientIDQuery;
import net.timewalker.ffmq3.transport.packet.query.StartConnectionQuery;
import net.timewalker.ffmq3.transport.packet.query.StopConnectionQuery;
import net.timewalker.ffmq3.transport.packet.query.UnsubscribeQuery;
import net.timewalker.ffmq3.transport.packet.response.AcknowledgeResponse;
import net.timewalker.ffmq3.transport.packet.response.CloseBrowserEnumerationResponse;
import net.timewalker.ffmq3.transport.packet.response.CloseBrowserResponse;
import net.timewalker.ffmq3.transport.packet.response.CloseConsumerResponse;
import net.timewalker.ffmq3.transport.packet.response.CloseSessionResponse;
import net.timewalker.ffmq3.transport.packet.response.CommitResponse;
import net.timewalker.ffmq3.transport.packet.response.CreateBrowserResponse;
import net.timewalker.ffmq3.transport.packet.response.CreateConsumerResponse;
import net.timewalker.ffmq3.transport.packet.response.CreateSessionResponse;
import net.timewalker.ffmq3.transport.packet.response.CreateTemporaryQueueResponse;
import net.timewalker.ffmq3.transport.packet.response.CreateTemporaryTopicResponse;
import net.timewalker.ffmq3.transport.packet.response.DeleteTemporaryQueueResponse;
import net.timewalker.ffmq3.transport.packet.response.DeleteTemporaryTopicResponse;
import net.timewalker.ffmq3.transport.packet.response.ErrorResponse;
import net.timewalker.ffmq3.transport.packet.response.GetResponse;
import net.timewalker.ffmq3.transport.packet.response.OpenConnectionResponse;
import net.timewalker.ffmq3.transport.packet.response.PingResponse;
import net.timewalker.ffmq3.transport.packet.response.PrefetchResponse;
import net.timewalker.ffmq3.transport.packet.response.PutResponse;
import net.timewalker.ffmq3.transport.packet.response.QueueBrowserFetchElementResponse;
import net.timewalker.ffmq3.transport.packet.response.QueueBrowserGetEnumerationResponse;
import net.timewalker.ffmq3.transport.packet.response.RecoverResponse;
import net.timewalker.ffmq3.transport.packet.response.RollbackMessageResponse;
import net.timewalker.ffmq3.transport.packet.response.RollbackResponse;
import net.timewalker.ffmq3.transport.packet.response.SetClientIDResponse;
import net.timewalker.ffmq3.transport.packet.response.StartConnectionResponse;
import net.timewalker.ffmq3.transport.packet.response.StopConnectionResponse;
import net.timewalker.ffmq3.transport.packet.response.UnsubscribeResponse;
import net.timewalker.ffmq3.utils.watchdog.ActiveObject;
import net.timewalker.ffmq3.utils.watchdog.ActivityWatchdog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ClientProcessor
 */
public final class ClientProcessor implements PacketTransportListener, ActiveObject, ClientProcessorMBean
{
	private static final Log log = LogFactory.getLog(ClientProcessor.class);

	// Attributes
	private String id;
	private AbstractClientListener parentListener;
	private FFMQEngine engine;
	protected PacketTransport transport;
	private int authTimeout; // seconds
	
	// Runtime
    private LocalConnection localConnection;
    private boolean traceEnabled;
    private long lastActivity;
    private boolean hasCreatedASession;
    
    /**
	 * Constructor
	 */
	public ClientProcessor( String id , AbstractClientListener parentListener , FFMQEngine engine , PacketTransport transport )
	{
		this.id = id;
		this.parentListener = parentListener;
		this.engine = engine;
		this.transport = transport;
		this.transport.setListener(this);
		this.traceEnabled = log.isTraceEnabled();
		this.authTimeout = engine.getSetup().getSettings().getIntProperty(FFMQServerSettings.LISTENER_AUTH_TIMEOUT,5);
		this.lastActivity = System.currentTimeMillis();
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ClientProcessorMBean#getClientID()
	 */
	public String getClientID()
	{
		return id;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ClientProcessorMBean#getPeerDescription()
	 */
	public String getPeerDescription()
	{
		return transport.getRemotePeerID();
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ClientProcessorMBean#isAuthenticated()
	 */
	public boolean isAuthenticated()
	{
		return localConnection != null;
	}

	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ClientProcessorMBean#getSessionsCount()
	 */
	public int getSessionsCount()
	{
		return localConnection != null ? localConnection.getSessionsCount() : 0;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ClientProcessorMBean#getProducersCount()
	 */
	public int getProducersCount()
	{
		return localConnection != null ? localConnection.getProducersCount() : 0;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ClientProcessorMBean#getConsumersCount()
	 */
	public int getConsumersCount()
	{
		return localConnection != null ? localConnection.getConsumersCount() : 0;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.listeners.ClientProcessorMBean#getEntitiesDescription()
	 */
	public String getEntitiesDescription()
	{
		if (localConnection == null)
			return "Not authenticated";
		
		StringBuffer sb = new StringBuffer(100);
		localConnection.getEntitiesDescription(sb);
		return sb.toString();
	}
	
	/**
     * Start the processor
     */
    public void start() throws PacketTransportException
    {
    	ActivityWatchdog.getInstance().register(this);
        transport.start();
    }
    
    /**
     * Stop the processor
     */
    public void stop()
    {
    	transport.close();
    	ActivityWatchdog.getInstance().unregister(this);
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.watchdog.ActiveObject#getLastActivity()
     */
    public long getLastActivity()
    {
    	return lastActivity;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.listeners.ClientProcessorMBean#getConnectionDate()
     */
    public Date getConnectionDate()
    {
    	return new Date(lastActivity);
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.watchdog.ActiveObject#getTimeoutDelay()
     */
    public long getTimeoutDelay()
    {
    	return (long)authTimeout*1000;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.watchdog.ActiveObject#onActivityTimeout()
     */
    public boolean onActivityTimeout() throws Exception
    {
    	if (!transport.isClosed())
    	{
    		log.warn("#"+id+" Timeout waiting for client activity ("+authTimeout+"s), dropping client.");
    		stop();
    	}
    	return true;
    }
    
	/*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.remote.transport.PacketTransportListener#packetReceived(net.timewalker.ffmq3.remote.transport.packet.AbstractPacket)
     */
    public boolean packetReceived(AbstractPacket packet)
    {
        AbstractQueryPacket query = (AbstractQueryPacket)packet;
    	AbstractResponsePacket response = null;
    	
    	// Process packet
        try
        {
            try
            {
                response = process(query);
            }
            catch (JMSException e)
            {
            	log.debug("#"+id+" process() failed with "+e.toString());
                response = new ErrorResponse(e);
            }
        }
        catch (Exception e)
        {
            log.error("#"+id+" Cannot process command",e);
        }
        
        // Send response
        if (response != null && (query.getEndpointId() != -1 || response instanceof PingResponse))
        {        	
            // Map endpoint id on response
            response.setEndpointId(query.getEndpointId());
            
	        try
	        {   
	            if (traceEnabled)
	                log.trace("#"+id+" Sending "+response);
	            transport.send(response);
	        }
	        catch (Exception e)
	        {
	            log.warn("#"+id+" Cannot send response to client : "+e.toString());
	            transport.close();
	        }
        }
        
        return localConnection != null; // Connection still valid ?
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.remote.transport.PacketTransportListener#packetSent(net.timewalker.ffmq3.remote.transport.packet.AbstractPacket)
     */
    public void packetSent(AbstractPacket packet)
    {
        if (traceEnabled)
            log.trace("#"+id+" Sent "+packet);
    }
    
	/* (non-Javadoc)
     * @see net.timewalker.ffmq3.remote.transport.PacketTransportListener#transportClosed(boolean)
     */
    public void transportClosed(boolean closedByRemotePeer)
    {
    	parentListener.unregisterClient(this);
        try
        {
            if (localConnection != null)
                localConnection.close();
        }
        catch (Exception e)
        {
            log.error("#"+id+" Could not close local connection",e);
        }
        finally
        {
        	localConnection = null;
        }
    }
	
    /**
     * Process an incoming packet
     */
	protected AbstractResponsePacket process( AbstractQueryPacket query ) throws JMSException
    {
    	switch (query.getType())
    	{
    		case PacketType.Q_GET :                 return processGet((GetQuery)query);
    		case PacketType.Q_PUT :                 return processPut((PutQuery)query);
    		case PacketType.Q_COMMIT :              return processCommit((CommitQuery)query);
    		case PacketType.Q_ACKNOWLEDGE :         return processAcknowledge((AcknowledgeQuery)query);
    		case PacketType.Q_ROLLBACK :            return processRollback((RollbackQuery)query);
    		case PacketType.Q_RECOVER :             return processRecover((RecoverQuery)query);
    		case PacketType.Q_CREATE_SESSION :      return processCreateSession((CreateSessionQuery)query);
    		case PacketType.Q_CLOSE_SESSION :       return processCloseSession((CloseSessionQuery)query);
    		case PacketType.Q_CREATE_CONSUMER:      return processCreateConsumer((CreateConsumerQuery)query);
    		case PacketType.Q_CREATE_DURABLE_SUBSCRIBER : return processCreateDurableSubscriber((CreateDurableSubscriberQuery)query);
    		case PacketType.Q_CREATE_BROWSER :      return processCreateBrowser((CreateBrowserQuery)query);
    		case PacketType.Q_CREATE_BROWSER_ENUM : return processQueueBrowserGetEnumeration((QueueBrowserGetEnumerationQuery)query);
    		case PacketType.Q_BROWSER_ENUM_FETCH :  return processQueueBrowserFetchElement((QueueBrowserFetchElementQuery)query);
    		case PacketType.Q_CLOSE_CONSUMER :      return processCloseConsumer((CloseConsumerQuery)query);
    		case PacketType.Q_CLOSE_BROWSER :       return processCloseBrowser((CloseBrowserQuery)query);
    		case PacketType.Q_CLOSE_BROWSER_ENUM :  return processCloseBrowserEnumeration((CloseBrowserEnumerationQuery)query);
    		case PacketType.Q_CREATE_TEMP_QUEUE :   return processCreateTemporaryQueue((CreateTemporaryQueueQuery)query);
    		case PacketType.Q_CREATE_TEMP_TOPIC :   return processCreateTemporaryTopic((CreateTemporaryTopicQuery)query);
    		case PacketType.Q_DELETE_TEMP_QUEUE :   return processDeleteTemporaryQueue((DeleteTemporaryQueueQuery)query);
    		case PacketType.Q_DELETE_TEMP_TOPIC :   return processDeleteTemporaryTopic((DeleteTemporaryTopicQuery)query);
    		case PacketType.Q_OPEN_CONNECTION :     return processOpenConnection((OpenConnectionQuery)query);
    		case PacketType.Q_START_CONNECTION :    return processStartConnection((StartConnectionQuery)query);
    		case PacketType.Q_STOP_CONNECTION :     return processStopConnection((StopConnectionQuery)query);
    		case PacketType.Q_SET_CLIENT_ID :       return processSetClientID((SetClientIDQuery)query);
    		case PacketType.Q_UNSUBSCRIBE :         return processUnsubscribe((UnsubscribeQuery)query);
    		case PacketType.Q_PREFETCH :            return processPrefetch((PrefetchQuery)query);
    		case PacketType.Q_PING :                return processPing((PingQuery)query);
    		case PacketType.Q_ROLLBACK_MESSAGE :    return processRollbackMessage((RollbackMessageQuery)query);
    		
    		default:
    			throw new javax.jms.IllegalStateException("Unkown query type id : "+query.getType());
    	}
    }
    
	private LocalConnection getLocalConnection() throws JMSException
    {
        if (localConnection == null)
            throw new FFMQException("Connection not established","NETWORK_ERROR");
        
        return localConnection;
    }
	
    private CreateSessionResponse processCreateSession( CreateSessionQuery query ) throws JMSException
    {
        // Note : acknowledgeMode is forced to CLIENT_ACKNOWLEDGE because we need the autoacknowledge feature
        //        to happen on the remote side
        LocalSession localSession = (LocalSession)getLocalConnection().createSession(query.getSessionId(),
        		                                                                     query.isTransacted(),
                                                                                     Session.CLIENT_ACKNOWLEDGE
                                                                                     /*query.getAcknowledgeMode()*/);
        
        // Unregister client inactivity watchdog
        if (!hasCreatedASession)
        {
        	hasCreatedASession = true;
        	ActivityWatchdog.getInstance().unregister(this);
        }
        
        // Use an internal hook to bridge the local session to the remote peer
        localSession.setNotificationProxy(new RemoteNotificationProxy(localSession.getId(),transport));
        
        return new CreateSessionResponse();
    }
    
    private LocalSession lookupSession( AbstractSessionQuery query ) throws JMSException
    {
        LocalSession localSession = (LocalSession)getLocalConnection().lookupRegisteredSession(query.getSessionId());
        if (localSession == null)
            throw new FFMQException("Invalid session id : "+query.getSessionId(),"NETWORK_ERROR");
        return localSession;
    }
    
    private LocalMessageConsumer lookupConsumer( AbstractConsumerQuery query ) throws JMSException
    {
        LocalSession localSession = lookupSession(query);
        LocalMessageConsumer consumer = (LocalMessageConsumer)localSession.lookupRegisteredConsumer(query.getConsumerId());
        if (consumer == null)
            throw new FFMQException("Invalid consumer id : "+query.getConsumerId(),"NETWORK_ERROR");
        return consumer;
    }
    
    private LocalQueueBrowser lookupBrowser( AbstractQueueBrowserQuery query ) throws JMSException
    {
        LocalSession localSession = lookupSession(query);
        LocalQueueBrowser browser = (LocalQueueBrowser)localSession.lookupRegisteredBrowser(query.getBrowserId());
        if (browser == null)
            throw new FFMQException("Invalid browser id : "+query.getBrowserId(),"NETWORK_ERROR");
        return browser;
    }
    
    private LocalQueueBrowserEnumeration lookupBrowserEnumeration( AbstractQueueBrowserEnumerationQuery query ) throws JMSException
    {
        LocalQueueBrowser browser = lookupBrowser(query);
        LocalQueueBrowserEnumeration browserEnum = (LocalQueueBrowserEnumeration)browser.lookupRegisteredEnumeration(query.getEnumId());
        if (browserEnum == null)
            throw new FFMQException("Invalid browser enumeration id : "+query.getEnumId(),"NETWORK_ERROR");
        return browserEnum;
    }
    
    private CloseSessionResponse processCloseSession( CloseSessionQuery query ) throws JMSException
    {
        Session localSession = lookupSession(query);
        localSession.close();

        return new CloseSessionResponse();
    }
    
    private CommitResponse processCommit( CommitQuery query ) throws JMSException
    {
        LocalSession localSession = lookupSession(query);
                
        // Commit session
        List deliveredMessageIDs = query.getDeliveredMessageIDs();
        localSession.commit(deliveredMessageIDs != null && !deliveredMessageIDs.isEmpty(),
        		            deliveredMessageIDs);
        
        return new CommitResponse();
    }
    
    private RollbackResponse processRollback( RollbackQuery query ) throws JMSException
    {
    	LocalSession localSession = lookupSession(query);
    	List deliveredMessageIDs = query.getDeliveredMessageIDs();
        localSession.rollback(deliveredMessageIDs != null && !deliveredMessageIDs.isEmpty(), deliveredMessageIDs);
        
        return new RollbackResponse();
    }
    
    private GetResponse processGet( GetQuery query ) throws JMSException
    {
        LocalMessageConsumer consumer = lookupConsumer(query);
        Message msg = consumer.receiveFromDestination(0,false);     
        GetResponse response = new GetResponse();
        response.setMessage((AbstractMessage)msg);
        
        return response;
    }
    
    private AbstractResponsePacket processPrefetch( PrefetchQuery query ) throws JMSException
    {
        LocalMessageConsumer consumer = lookupConsumer(query);
        consumer.prefetchMore();

        return new PrefetchResponse();
    }
    
    private PutResponse processPut( PutQuery query ) throws JMSException
    {
        LocalSession session = lookupSession(query);
        AbstractMessage msg = query.getMessage();
        
        // Dispatch to session
        session.dispatch(msg);
     
        return new PutResponse();
    }
    
    private AcknowledgeResponse processAcknowledge( AcknowledgeQuery query ) throws JMSException
    {
        LocalSession session = lookupSession(query);
        session.acknowledge(query.getDeliveredMessageIDs());
        
        return new AcknowledgeResponse();
    }
    
    private RecoverResponse processRecover( RecoverQuery query ) throws JMSException
    {
        LocalSession session = lookupSession(query);
        session.recover(query.getDeliveredMessageIDs());
        
        return new RecoverResponse();
    }
    
    private CreateBrowserResponse processCreateBrowser( CreateBrowserQuery query ) throws JMSException
    {
        LocalSession session = lookupSession(query);
                
        session.createBrowser(query.getBrowserId(),
        		              query.getQueue(),
                              query.getMessageSelector());
        
        return new CreateBrowserResponse();
    }
    
    private QueueBrowserGetEnumerationResponse processQueueBrowserGetEnumeration( QueueBrowserGetEnumerationQuery query ) throws JMSException
    {
        LocalQueueBrowser browser = lookupBrowser(query);
        LocalQueueBrowserEnumeration browserEnum = (LocalQueueBrowserEnumeration)browser.getEnumeration();
        
        QueueBrowserGetEnumerationResponse response = new QueueBrowserGetEnumerationResponse();
        response.setEnumId(browserEnum.getId());

        return response;
    }
    
    private QueueBrowserFetchElementResponse processQueueBrowserFetchElement( QueueBrowserFetchElementQuery query ) throws JMSException
    {
        LocalQueueBrowserEnumeration browserEnum = lookupBrowserEnumeration(query);
        
        QueueBrowserFetchElementResponse response = new QueueBrowserFetchElementResponse();
        if (browserEnum.hasMoreElements())
        	response.setMessage((AbstractMessage)browserEnum.nextElement());
        else
        	response.setMessage(null);

        return response;
    }
    
    private CloseConsumerResponse processCloseConsumer( CloseConsumerQuery query ) throws JMSException
    {
        LocalMessageConsumer consumer = lookupConsumer(query);
        consumer.close();
        
        // Rollback undelivered prefetched messages
        List undeliveredMessageIDs = query.getUndeliveredMessageIDs();
        if (undeliveredMessageIDs != null && !undeliveredMessageIDs.isEmpty())
        	((LocalSession)consumer.getSession()).rollbackUndelivered(undeliveredMessageIDs);

        return new CloseConsumerResponse();
    }
    
    private RollbackMessageResponse processRollbackMessage( RollbackMessageQuery query ) throws JMSException
    {
    	LocalConnection localConnection = getLocalConnection();
    	
        LocalSession localSession = (LocalSession)localConnection.lookupRegisteredSession(query.getSessionId());
        if (localSession != null)
        {
            // Rollback undelivered prefetched messages
            List undeliveredMessageIDs = new ArrayList();
            undeliveredMessageIDs.add(query.getMessageId());
            localSession.rollbackUndelivered(undeliveredMessageIDs);
            
            LocalMessageConsumer consumer = (LocalMessageConsumer)localSession.lookupRegisteredConsumer(query.getConsumerId());
            if (consumer != null)
            	consumer.restorePrefetchCapacity(1);
        }
        
        return new RollbackMessageResponse();
    }
    
    private CloseBrowserResponse processCloseBrowser( CloseBrowserQuery query ) throws JMSException
    {
        LocalQueueBrowser browser = lookupBrowser(query);
        
        browser.close();

        return new CloseBrowserResponse();
    }
    
    private CloseBrowserEnumerationResponse processCloseBrowserEnumeration( CloseBrowserEnumerationQuery query ) throws JMSException
    {
        LocalQueueBrowserEnumeration browserEnum = lookupBrowserEnumeration(query);
        
        browserEnum.close();

        return new CloseBrowserEnumerationResponse();
    }
    
    private CreateConsumerResponse processCreateConsumer( CreateConsumerQuery query ) throws JMSException
    {
        LocalSession session = lookupSession(query);
        LocalMessageConsumer consumer = (LocalMessageConsumer)session.createConsumer(query.getConsumerId(),
        		                                                                     query.getDestination(),
                                                                                     query.getMessageSelector(),
                                                                                     query.isNoLocal());
        
        // Start prefetching if we are receiving from a queue
        if (query.getDestination() instanceof Queue)
        	consumer.prefetchMore();
        
        CreateConsumerResponse response = new CreateConsumerResponse();
        response.setPrefetchSize(consumer.getPrefetchSize());
        
        return response;
    }
    
    private CreateTemporaryQueueResponse processCreateTemporaryQueue( CreateTemporaryQueueQuery query ) throws JMSException
    {
        LocalSession session = lookupSession(query);
        TemporaryQueue queue = session.createTemporaryQueue();
        
        CreateTemporaryQueueResponse response = new CreateTemporaryQueueResponse();
        response.setQueueName(queue.getQueueName());
        return response;
    }
    
    private CreateTemporaryTopicResponse processCreateTemporaryTopic( CreateTemporaryTopicQuery query ) throws JMSException
    {
        LocalSession session = lookupSession(query);
        TemporaryTopic topic = session.createTemporaryTopic();
        
        CreateTemporaryTopicResponse response = new CreateTemporaryTopicResponse();
        response.setTopicName(topic.getTopicName());
        return response;
    }
    
    private DeleteTemporaryQueueResponse processDeleteTemporaryQueue( DeleteTemporaryQueueQuery query ) throws JMSException
    {
        getLocalConnection().deleteTemporaryQueue(query.getQueueName());
        return new DeleteTemporaryQueueResponse();
    }
    
    private DeleteTemporaryTopicResponse processDeleteTemporaryTopic( DeleteTemporaryTopicQuery query ) throws JMSException
    {
        getLocalConnection().deleteTemporaryTopic(query.getTopicName());
        return new DeleteTemporaryTopicResponse();
    }
    
    private OpenConnectionResponse processOpenConnection( OpenConnectionQuery query ) throws JMSException
    {
        if (localConnection != null)
            throw new FFMQException("Connection already established","NETWORK_ERROR");
            
        this.localConnection = 
            (LocalConnection)engine.openConnection(query.getUserName(),
                                                   query.getPassword(),
                                                   query.getClientID());
        
        OpenConnectionResponse response = new OpenConnectionResponse();
        response.setProtocolVersion(FFMQConstants.TRANSPORT_PROTOCOL_VERSION);
        return response;
    }
    
    private StartConnectionResponse processStartConnection( StartConnectionQuery query ) throws JMSException
    {
        getLocalConnection().start();
        return new StartConnectionResponse();
    }
    
    private StopConnectionResponse processStopConnection( StopConnectionQuery query ) throws JMSException
    {
        getLocalConnection().stop();
        return new StopConnectionResponse();
    }
    
    private SetClientIDResponse processSetClientID( SetClientIDQuery query ) throws JMSException
    {
        getLocalConnection().setClientID(query.getClientID());
        return new SetClientIDResponse();
    }
    
    private CreateConsumerResponse processCreateDurableSubscriber( CreateDurableSubscriberQuery query ) throws JMSException
    {
        LocalSession session = lookupSession(query);
        LocalDurableTopicSubscriber subscriber = (LocalDurableTopicSubscriber)session.createDurableSubscriber(query.getConsumerId(),
        		                                                                                              query.getTopic(), 
                                                                                                              query.getName(), 
                                                                                                              query.getMessageSelector(),
                                                                                                              query.isNoLocal());
        
        // Start prefetching
        subscriber.prefetchMore();
        
        CreateConsumerResponse response = new CreateConsumerResponse();
        response.setPrefetchSize(subscriber.getPrefetchSize());
        
        return response;
    }
    
    private UnsubscribeResponse processUnsubscribe( UnsubscribeQuery query ) throws JMSException
    {
    	LocalSession session = lookupSession(query);
    	session.unsubscribe(query.getSubscriptionName());

        return new UnsubscribeResponse();
    }
    
    private PingResponse processPing( PingQuery query ) throws JMSException
    {
    	getLocalConnection(); // Make sure a connection was established
    	return new PingResponse();
    }
}
