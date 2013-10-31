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
package net.timewalker.ffmq4.admin;

import java.util.Enumeration;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;

import net.timewalker.ffmq4.FFMQAdminConstants;
import net.timewalker.ffmq4.FFMQConstants;
import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.FFMQServer;
import net.timewalker.ffmq4.local.FFMQEngine;
import net.timewalker.ffmq4.local.connection.LocalQueueConnection;
import net.timewalker.ffmq4.local.destination.LocalQueue;
import net.timewalker.ffmq4.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq4.management.destination.definition.TopicDefinition;
import net.timewalker.ffmq4.utils.ErrorTools;
import net.timewalker.ffmq4.utils.Settings;
import net.timewalker.ffmq4.utils.StringTools;
import net.timewalker.ffmq4.utils.concurrent.SynchronizableThread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AdministrationThread
 */
public final class RemoteAdministrationThread extends SynchronizableThread
{
    private static final Log log          = LogFactory.getLog(RemoteAdministrationThread.class);

    protected FFMQServer     server;
    private FFMQEngine       engine;
    private QueueConnection  conn         = null;
    private QueueSession     session      = null;
    private QueueSender      sender       = null;
    private QueueReceiver    receiver     = null;
    private boolean          stopRequired = false;
    
    /**
     * Constructor
     */
    public RemoteAdministrationThread(FFMQServer server,FFMQEngine engine)
    {
        super("FFMQ-RemoteAdminThread");
        this.server = server;
        this.engine = engine;
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.utils.concurrent.SynchronizableThread#run()
     */
    @Override
	public void run()
    {
        log.info("Starting remote administration thread ...");

        try
        {
            LocalQueue inputQueue = engine.getLocalQueue(FFMQConstants.ADM_REQUEST_QUEUE);
            LocalQueue outputQueue = engine.getLocalQueue(FFMQConstants.ADM_REPLY_QUEUE);
            
            conn = new LocalQueueConnection(engine, null, null);
            session = conn.createQueueSession(true, Session.SESSION_TRANSACTED);
            receiver = session.createReceiver(inputQueue);
            sender = session.createSender(outputQueue);

            conn.start();

            // Flush input queue on startup
            inputQueue.purge(null);
            outputQueue.purge(null);
            
            // Enter listening loop
            notifyStartup();
            while (!stopRequired)
            {
                Message message = receiver.receive();
                if (message == null)
                    break; // Interrupted
                log.debug("Received message " + message);

                try
                {
                    // Process the command
                    String errorMsg = process(message);
    
                    // Build response message
                    Message response = session.createMessage();
                    response.setJMSCorrelationID(message.getJMSMessageID());
                    if (errorMsg != null)
                        response.setStringProperty(FFMQAdminConstants.ADM_HEADER_ERRMSG, errorMsg);
                    
                    sender.send(response,DeliveryMode.NON_PERSISTENT,Message.DEFAULT_PRIORITY,Message.DEFAULT_TIME_TO_LIVE);
                }
                catch (JMSException e)
                {
                    log.error("Cannot process admin command",e);
                }
                finally
                {
                    session.commit();
                }
            }
            
            log.debug("Remote administration thread has stopped");
        }
        catch (Throwable e)
        {
            log.fatal("Administration thread failed", e);
            notifyStartup();
        }
        finally
        {
            try
            {
                if (sender != null)
                    sender.close();
            }
            catch (JMSException e)
            {
            	ErrorTools.log(e, log);
            }

            try
            {
                if (receiver != null)
                    receiver.close();
            }
            catch (JMSException e)
            {
            	ErrorTools.log(e, log);
            }

            try
            {
                if (session != null)
                    session.close();
            }
            catch (JMSException e)
            {
            	ErrorTools.log(e, log);
            }

            try
            {
                if (conn != null)
                    conn.close();
            }
            catch (JMSException e)
            {
            	ErrorTools.log(e, log);
            }
        }
    }

    private String process(Message msg)
    {
        try
        {
            String command = msg.getStringProperty(FFMQAdminConstants.ADM_HEADER_COMMAND);
            if (StringTools.isEmpty(command))
                return "Administration command not set in message header";
                
            // Dispatch
            if (command.equals(FFMQAdminConstants.ADM_COMMAND_CREATE_QUEUE))
                return processCreateQueue(msg);
            
            if (command.equals(FFMQAdminConstants.ADM_COMMAND_CREATE_TOPIC))
                return processCreateTopic(msg);
            
            if (command.equals(FFMQAdminConstants.ADM_COMMAND_DELETE_QUEUE))
                return processDeleteQueue(msg);
            
            if (command.equals(FFMQAdminConstants.ADM_COMMAND_DELETE_TOPIC))
                return processDeleteTopic(msg);
            
            if (command.equals(FFMQAdminConstants.ADM_COMMAND_PURGE_QUEUE))
                return processPurgeQueue(msg);
            
            if (command.equals(FFMQAdminConstants.ADM_COMMAND_SHUTDOWN))
                return processShutdown();

            log.error("Invalid administration command : "+command);
            return "Invalid administration command : "+command;
        }
        catch (JMSException e)
        {
        	ErrorTools.log(e, log);
        	return "Error processing administration command : "+e.getMessage();
        }
        catch (Exception e)
        {
        	ErrorTools.log(new FFMQException("Cannot process admin message","INVALID_ADMIN_MESSAGE",e), log);
            return "Error processing administration command : "+e.getMessage();
        }
    }
    
    private Settings createSettings( Message msg ) throws JMSException
    {
        // Fill settings from message headers
        Settings queueSettings = new Settings();
        Enumeration<?> headers = msg.getPropertyNames();
        while (headers.hasMoreElements())
        {
            String propName = (String)headers.nextElement();
            if (propName.startsWith(FFMQAdminConstants.ADM_HEADER_PREFIX))
                continue;
            
            String propValue = msg.getStringProperty(propName);
            queueSettings.setStringProperty(propName, propValue);
        }
        return queueSettings;
    }
    
    private String processCreateQueue( Message msg ) throws JMSException
    {
        Settings queueSettings = createSettings(msg);
        QueueDefinition queueDef = new QueueDefinition(queueSettings);
        
        log.debug("Creating queue : "+queueDef);
        engine.createQueue(queueDef);
        
        // Success
        return null;
    }
    
    private String processCreateTopic( Message msg ) throws JMSException
    {
        Settings topicSettings = createSettings(msg);
        TopicDefinition topicDef = new TopicDefinition(topicSettings);

        log.debug("Creating topic : "+topicDef);
        engine.createTopic(topicDef);
        
        // Success
        return null;
    }
    
    private String processDeleteQueue( Message msg ) throws JMSException
    {
        String destName = msg.getStringProperty("name");
        if (StringTools.isEmpty(destName))
            return "Destination name not specified";
        
        if (!engine.localQueueExists(destName))
            return "Queue "+destName+" does not exist";
        
        engine.deleteQueue(destName);
        
        return null;
    }
    
    private String processDeleteTopic( Message msg ) throws JMSException
    {
        String destName = msg.getStringProperty("name");
        if (StringTools.isEmpty(destName))
            return "Destination name not specified";
        
        if (!engine.localTopicExists(destName))
            return "Topic "+destName+" does not exist";
        
        engine.deleteTopic(destName);
        
        return null;
    }
    
    private String processPurgeQueue( Message msg ) throws JMSException
    {
        String destName = msg.getStringProperty("name");
        if (StringTools.isEmpty(destName))
            return "Destination name not specified";
        
        if (!engine.localQueueExists(destName))
            return "Queue "+destName+" does not exist";
        
        LocalQueue queue = engine.getLocalQueue(destName);
        queue.purge(null);
        
        return null;
    }
    
    private String processShutdown()
    {
    	// Shutdown the server
    	if (server.isInRunnableMode())
    		server.pleaseStop();
    	else
    	{
    		// Run the shutdown sequence in a parallel thread to avoid deadlocks
    		new Thread() {
	    		@Override
				public void run() {
	    			server.shutdown();
	    		}
	    	}.start();
    	}
    	
        return null;
    }
    
    /**
     * Ask the thread to stop
     */
    public void pleaseStop()
    {
    	if (stopRequired)
    		return;
    	
        stopRequired = true;
        try
        {
            if (receiver != null)
                receiver.close();
        }
        catch (JMSException e)
        {
        	ErrorTools.log(e, log);
        }
    }
}
