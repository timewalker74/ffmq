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
package net.timewalker.ffmq4.test;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import net.timewalker.ffmq4.FFMQConstants;

/**
 * TestConsumerClient
 */
public class TestMultiServerConsumerClient implements Runnable
{
	//private static final String providerURL = TestUtils.TCP_TRANSPORT_URI;
	private static final String[] providerURLs = { 
		"tcpnio://192.168.2.26:10101",
		"tcpnio://192.168.2.26:10102",
		"tcpnio://192.168.2.26:10103",
		"tcpnio://192.168.2.26:10002"
	};
	
	private static final boolean USE_QUEUE = true;
	
	private boolean stopRequired;
	private Connection[] connections = new Connection[providerURLs.length];
	private Session[] sessions = new Session[providerURLs.length];
	private MessageConsumer[] consumers = new MessageConsumer[providerURLs.length];
	private int received = 0;

    protected void onJMSException(int rank,JMSException exception)
	{
    	System.out.println("IN EXCEPTION LISTENER ("+rank+")");
		try {
			Thread.sleep(120 * 1000);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
    	
    	System.out.println(">> "+rank);
		exception.printStackTrace();
		closeResources(rank);
		while (true)
		{
			try
			{
				connectAndListen(rank, providerURLs[rank]);
				break;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				try
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}
	
    protected void onJMSMessage(int rank, Message message)
    {
    	System.out.println("["+rank+"] "+message);
    }
    
	private void closeResources(int rank)
	{
		try
		{
			if (connections[rank] != null)
				connections[rank].close();
		}
		catch (JMSException e1)
		{
			e1.printStackTrace();
		}
		finally
		{
			connections[rank] = null;
			sessions[rank] = null;
			consumers[rank] = null;
		}
	}
	
	private Connection connect( String providerURL , ExceptionListener exceptionListener ) throws Exception
	{
		Hashtable<String,Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, FFMQConstants.JNDI_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, providerURL);
        Context context = new InitialContext(env);
        
        ConnectionFactory connFactory = (ConnectionFactory)context.lookup(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME);
        
        Connection conn = connFactory.createConnection("test","test");
        conn.setExceptionListener(exceptionListener);
        conn.start();
        
        return conn;
	}
	
	private MessageConsumer connectAndListen( final int rank , String providerURL ) throws Exception
	{
		System.out.println("Connecting to "+providerURL+" ("+rank+")");
		ExceptionListener exceptionListener = new ExceptionListener() {
			/*
			 * (non-Javadoc)
			 * @see javax.jms.ExceptionListener#onException(javax.jms.JMSException)
			 */
			@Override
			public void onException(JMSException exception)
			{
				onJMSException(rank, exception);
			}
		};
		Connection conn = connect(providerURL, exceptionListener);
		connections[rank] = conn;
		
		Session session = conn.createSession(true,Session.SESSION_TRANSACTED);
		sessions[rank] = session;
		
        Destination destination = USE_QUEUE ? (Destination)session.createQueue("TEST") : (Destination)session.createTopic("VOLATILE");
        MessageConsumer consumer = session.createConsumer(destination);
        consumers[rank] = consumer;
        
        consumer.setMessageListener(new MessageListener() {
			/*
			 * (non-Javadoc)
			 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
			 */
        	@Override
			public void onMessage(Message message)
			{
				onJMSMessage(rank, message);
			}
		});
        
        return consumer;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try
		{
			for(int n=0;n<providerURLs.length;n++)
				connectAndListen(n, providerURLs[n]);
	
			while (!stopRequired)
				Thread.sleep(1000);
			
			System.out.println("Closing all connections ...");
			for(int n=0;n<providerURLs.length;n++)
				closeResources(n);
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
		}
	}
	
	public static void main(String[] args) throws Exception
    {
		new TestMultiServerConsumerClient().run();
    }
}
