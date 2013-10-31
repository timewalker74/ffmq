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
package net.timewalker.ffmq3;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.timewalker.ffmq3.transport.PacketTransportType;
import net.timewalker.ffmq3.utils.Settings;
import net.timewalker.ffmq3.utils.StringTools;

/**
 * FFMQAdminClient
 */
public final class FFMQAdminClient implements Runnable
{
    private Settings globalSettings;
    private Settings paramSettings;
    
    // Runtime
    private Connection connection;
    private Session session;
    
    // Output
    private PrintStream out;
    private PrintStream err;
    
    /**
     * Constructor
     */
    public FFMQAdminClient( Settings globalSettings , Settings paramSettings , PrintStream out , PrintStream err )
    {
        this.globalSettings = globalSettings;
        this.paramSettings = paramSettings;
        this.out = out;
        this.err = err;
    }
    
    private void logError( JMSException e )
    {
    	if (e.getErrorCode() != null)
    		err.println("error={"+e.getErrorCode()+"} "+e.getMessage());
    	else
    		err.println(e.getMessage());
    	if (e.getLinkedException() != null)
    	{
    		err.println("Linked exception was : ");
    		e.getLinkedException().printStackTrace(err);
    	}
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        String command = globalSettings.getStringProperty(FFMQAdminClientSettings.ADM_COMMAND, null);
        if (StringTools.isEmpty(command))
        {
            err.println("No command specified");
            return;
        }
        
        try
        {
            openSession();
            processCommand(command);
        }
        catch (JMSException e)
        {
        	logError(e);
        }
        finally
        {
        	closeSession();
        }
    }
    
    private void openSession() throws JMSException
    {
        String serverHost   = globalSettings.getStringProperty(FFMQAdminClientSettings.SERVER_TCP_HOST, FFMQConstants.DEFAULT_SERVER_HOST);
        int serverPort      = globalSettings.getIntProperty(FFMQAdminClientSettings.SERVER_TCP_PORT, FFMQConstants.DEFAULT_SERVER_PORT);
        String userName     = globalSettings.getStringProperty(FFMQAdminClientSettings.ADMIN_USER_NAME,null);
        String userPassword = globalSettings.getStringProperty(FFMQAdminClientSettings.ADMIN_USER_PASSWORD,null);
        
        out.println("Opening connection to server "+serverHost+":"+serverPort+" for user "+userName);        
        this.connection = getConnectionFactory(serverHost,serverPort).createConnection(userName, userPassword);
        this.connection.start();
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }
    
    private void closeSession()
    {
        if (session != null)
        {
            try
            {
                session.close();
            }
            catch (JMSException e)
            {
            	logError(e);
            }
        }
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (JMSException e)
            {
            	logError(e);
            }
        }
    }
    
    private Context getJNDIContext( String serverHost , int serverPort ) throws NamingException
    {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, FFMQConstants.JNDI_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, PacketTransportType.TCP+"://"+serverHost+":"+serverPort);
        return new InitialContext(env);
    }
    
    private ConnectionFactory getConnectionFactory( String serverHost , int serverPort ) throws JMSException
    {
        try
        {
            Context context = getJNDIContext(serverHost,serverPort);
            return (ConnectionFactory)context.lookup(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME);
        }
        catch (NamingException e)
        {
            throw new FFMQException("Cannot lookup connection factory in JNDI context","JNDI_ERROR",e);
        }
    }
    
    private void processCommand( String command ) throws JMSException
    {
        // Dispatch
        if (command.equals(FFMQAdminConstants.ADM_COMMAND_CREATE_QUEUE))
            processCreateQueue();
        else if (command.equals(FFMQAdminConstants.ADM_COMMAND_CREATE_TOPIC))
            processCreateTopic();
        else if (command.equals(FFMQAdminConstants.ADM_COMMAND_DELETE_QUEUE))
            processDeleteQueue();
        else if (command.equals(FFMQAdminConstants.ADM_COMMAND_DELETE_TOPIC))
            processDeleteTopic();
        else if (command.equals(FFMQAdminConstants.ADM_COMMAND_PURGE_QUEUE))
            processPurgeQueue();
        else if (command.equals(FFMQAdminConstants.ADM_COMMAND_SHUTDOWN))
            processShutdown();
        else
            throw new FFMQException("Unknown command : " + command,"INVALID_ADMIN_COMMAND");
    }
    
    private void remoteExec( Message msg , boolean expectReply ) throws JMSException
    {
        // Send the request event
        Queue requestQueue = session.createQueue(FFMQConstants.ADM_REQUEST_QUEUE);
        MessageProducer producer = session.createProducer(requestQueue);
        producer.send(msg,DeliveryMode.NON_PERSISTENT,Message.DEFAULT_PRIORITY,Message.DEFAULT_TIME_TO_LIVE);
        String correlID = msg.getJMSMessageID();
        producer.close();
        
        if (expectReply)
        {
	        Queue replyQueue = session.createQueue(FFMQConstants.ADM_REPLY_QUEUE);
	        MessageConsumer consumer = session.createConsumer(replyQueue,"JMSCorrelationID='"+correlID+"'");
	        int requestTimeout = globalSettings.getIntProperty(FFMQAdminClientSettings.ADMIN_REQUEST_TIMEOUT, 30);
	        Message responseMsg = consumer.receive(requestTimeout*1000L);
	        consumer.close();
	        
	        if (responseMsg == null)
	            throw new FFMQException("Timeout waiting for server response after "+requestTimeout+" second(s)","NETWORK_ERROR");
	
	        String errorMsg = responseMsg.getStringProperty(FFMQAdminConstants.ADM_HEADER_ERRMSG);
	        if (StringTools.isNotEmpty(errorMsg))
	            throw new FFMQException("Command failed : "+errorMsg,"NETWORK_ERROR");
	        
	        out.println("Command sucessfully completed.");
        }
        else
        	out.println("Command sucessfully sent.");
    }
    
    private Message createCommandMessage( String commandName ) throws JMSException
    {
        Message msg = session.createMessage();
        msg.setStringProperty(FFMQAdminConstants.ADM_HEADER_COMMAND, commandName);
        Iterator keys = paramSettings.keySet().iterator();
        while (keys.hasNext())
        {
            String paramName = (String)keys.next();
            String paramValue = paramSettings.getStringProperty(paramName);
            msg.setStringProperty(paramName,paramValue);
        }
        return msg;
    }
    
    private void processCreateQueue() throws JMSException
    {
        String destinationName = paramSettings.getStringProperty("name", null);
        if (StringTools.isEmpty(destinationName))
            throw new FFMQException("Destination name not specified","INVALID_DESTINATION_NAME");
        
        out.println("Creating queue "+destinationName);

        // Create message
        Message msg = createCommandMessage(FFMQAdminConstants.ADM_COMMAND_CREATE_QUEUE);
        
        // Send message
        remoteExec(msg,true);
    }
    
    private void processCreateTopic() throws JMSException
    {
        String destinationName = paramSettings.getStringProperty("name", null);
        if (StringTools.isEmpty(destinationName))
            throw new FFMQException("Destination name not specified","INVALID_DESTINATION_NAME");
     
        out.println("Creating topic "+destinationName);
        
        // Create message
        Message msg = createCommandMessage(FFMQAdminConstants.ADM_COMMAND_CREATE_TOPIC);
        
        // Send message
        remoteExec(msg,true);
    }
    
    private void processDeleteQueue() throws JMSException
    {
        String destinationName = paramSettings.getStringProperty("name", null);
        if (StringTools.isEmpty(destinationName))
            throw new FFMQException("Destination name not specified","INVALID_DESTINATION_NAME");
        
        out.println("Deleting queue "+destinationName);
        
        // Create message
        Message msg = createCommandMessage(FFMQAdminConstants.ADM_COMMAND_DELETE_QUEUE);
        
        // Send message
        remoteExec(msg,true);
    }
    
    private void processDeleteTopic() throws JMSException
    {
        String destinationName = paramSettings.getStringProperty("name", null);
        if (StringTools.isEmpty(destinationName))
            throw new FFMQException("Destination name not specified","INVALID_DESTINATION_NAME");
        
        out.println("Deleting topic "+destinationName);

        // Create message
        Message msg = createCommandMessage(FFMQAdminConstants.ADM_COMMAND_DELETE_TOPIC);
        
        // Send message
        remoteExec(msg,true);
    }
    
    private void processPurgeQueue() throws JMSException
    {
        String destinationName = paramSettings.getStringProperty("name", null);
        if (StringTools.isEmpty(destinationName))
            throw new FFMQException("Destination name not specified","INVALID_DESTINATION_NAME");
        
        out.println("Purging queue "+destinationName);
        
        // Create message
        Message msg = createCommandMessage(FFMQAdminConstants.ADM_COMMAND_PURGE_QUEUE);
        
        // Send message
        remoteExec(msg,true);
    }
    
    private void processShutdown() throws JMSException
    {
        out.println("Asking the server to shutdown");
        
        // Create message
        Message msg = createCommandMessage(FFMQAdminConstants.ADM_COMMAND_SHUTDOWN);
        
        // Send message but do NOT wait for an answer
        remoteExec(msg,false);
    }
}
