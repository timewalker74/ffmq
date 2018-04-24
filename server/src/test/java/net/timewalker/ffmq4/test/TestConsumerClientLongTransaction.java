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
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import net.timewalker.ffmq4.FFMQConstants;

/**
 * TestConsumerClient
 */
public class TestConsumerClientLongTransaction implements Runnable, ExceptionListener
{
	private static final String providerURL = TestUtils.TCP_TRANSPORT_URI;
	//private static final String providerURL = "tcp://192.168.2.26:10101";
	
	private static final boolean USE_QUEUE = true;

	private Connection conn;
	private int received = 0;
    private int commited = 0;
    private boolean stopRequired = false;

    
    /* (non-Javadoc)
	 * @see javax.jms.ExceptionListener#onException(javax.jms.JMSException)
	 */
    @Override
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
	}
    
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
    @Override
	public void run()
	{
		try
		{
			Hashtable<String,Object> env = new Hashtable<>();
	        env.put(Context.INITIAL_CONTEXT_FACTORY, FFMQConstants.JNDI_CONTEXT_FACTORY);
	        env.put(Context.PROVIDER_URL, providerURL);
	        Context context = new InitialContext(env);
	        
	        ConnectionFactory connFactory = (ConnectionFactory)context.lookup(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME);
	        
	        conn = connFactory.createConnection("test","test");
	        conn.setExceptionListener(this);
	        conn.start();
	        
	        Session session = conn.createSession(true,Session.SESSION_TRANSACTED);
	        
	        
	        Destination destination = USE_QUEUE ? (Destination)session.createQueue("TEST") : (Destination)session.createTopic("VOLATILE");
	        
	        MessageConsumer consumer = session.createConsumer(destination);
	        
	        System.out.println("Listening on "+(USE_QUEUE ? "queue":"topic")+" TEST");
	        System.out.println("---------------------------------------------------------------------");
	        
	        for(int n=0;n<20;n++) {
	        	Message msg = consumer.receive(5000);
	        	if (msg != null)
	        	{
	        		System.out.println("Received : "+msg);
	        	}
	        	else
	        		System.out.println("Waiting ...");
	        	
	        	if (stopRequired)
	        		break;
	        }
	        
	        session.commit();
	        
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
		new TestConsumerClientLongTransaction().run();
    }
}
