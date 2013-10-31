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
package net.timewalker.ffmq3.test;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.spi.LoggingEvent;

import net.timewalker.ffmq3.FFMQConstants;

/**
 * TestListenerClient
 */
public class TestListenerClient implements Runnable, ExceptionListener, MessageListener
{
	private boolean stopRequired;
	private Connection conn;
	private int received = 0;
    private int commited = 0;
	
    /* (non-Javadoc)
	 * @see javax.jms.ExceptionListener#onException(javax.jms.JMSException)
	 */
	public void onException(JMSException e)
	{
		e.printStackTrace();
		stopRequired = true;
		try
		{
			conn.close();
		}
		catch (JMSException e1)
		{
			e1.printStackTrace();
		}
		synchronized (this)
        {
            notify();
        }
	}
    
	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message msg)
	{
		try
		{
			received++;
	        System.out.println(msg);
	        if (msg instanceof TextMessage)
	        	System.out.println(((TextMessage) msg).getText());
	        else
	        	if (msg instanceof ObjectMessage)
	        	{
	        		Object body = ((ObjectMessage) msg).getObject();
	        		if (body instanceof LoggingEvent)
	        			System.out.println(((LoggingEvent) body).getMessage());
	        	}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public synchronized void run()
	{
		try
		{
			Hashtable env = new Hashtable();
	        env.put(Context.INITIAL_CONTEXT_FACTORY, FFMQConstants.JNDI_CONTEXT_FACTORY);
	        env.put(Context.PROVIDER_URL, TestUtils.TCP_TRANSPORT_URI);
	        Context context = new InitialContext(env);
	        
	        ConnectionFactory connFactory = (ConnectionFactory)context.lookup(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME);
	        
	        conn = connFactory.createConnection();
	        conn.setExceptionListener(this);
	        
	        Session session = conn.createSession(false,Session.AUTO_ACKNOWLEDGE);
	        
	        //Destination destination = session.createQueue("TEST");
	        Destination destination = session.createTopic("TEST");
	        
	        MessageConsumer consumer = session.createConsumer(destination);
	        consumer.setMessageListener(this);
	        
	        System.out.println("---------------------------------------------------------------------");
	        conn.start();
	        
	        while (!stopRequired)
	        {
	        	wait();
	        }
	        
	        consumer.close();
	        session.close();
	        conn.close();
		}
		catch (Throwable e)
		{
			System.err.println("Consumer client failed");
			e.printStackTrace();
		}
		finally
		{
			System.out.println("---------------------------------------------------------------------");
	        System.out.println("Received = "+received);
	        System.out.println("Commited = "+commited);
		}
	}
	
	public static void main(String[] args) throws Exception
    {
		new TestListenerClient().run();
    }
}
