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
import java.util.Random;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import net.timewalker.ffmq4.FFMQConstants;

/**
 * TestCloseListenerConcurrency
 */
public class TestCloseListenerConcurrency implements Runnable, ExceptionListener, MessageListener
{
	private static final String PROVIDER_URL = "tcpnio://localhost:"+TestUtils.TEST_SERVER_PORT;
	
	protected boolean stopRequired;
	protected Connection conn;
	protected int received = 0;
	protected int commited = 0;
	protected int max = 0;
	protected Random random = new Random();
	protected Session session;
	 
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
    
	/*
	 * (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message msg)
	{
	    try
	    {
    	    received++;
    	    System.out.print(".");
    //        //System.out.println(msg);
            session.commit();
            commited++;
    //        
            if (received >= max)
            {
                session.close();
                System.out.println();
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
	@Override
	public void run()
	{
		try
		{
			Hashtable<String,Object> env = new Hashtable<>();
	        env.put(Context.INITIAL_CONTEXT_FACTORY, FFMQConstants.JNDI_CONTEXT_FACTORY);
	        env.put(Context.PROVIDER_URL, PROVIDER_URL);
	        Context context = new InitialContext(env);
	        
	        ConnectionFactory connFactory = (ConnectionFactory)context.lookup(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME);
	       
	        
	        do
			{
	        	received = 0;
	        	max = random.nextInt(2000);
	        	
		        conn = connFactory.createConnection();
		        conn.setExceptionListener(this);
		        conn.start();
		        
		        session = conn.createSession(true,Session.SESSION_TRANSACTED);
		        final Queue queue = session.createQueue("TEST");
		        final MessageConsumer consumer = session.createConsumer(queue);
		        
		        consumer.setMessageListener(this);
		        
		        Thread.sleep(random.nextInt(200));
		        
	//	        consumer.close();
	//	        session.close();
		        conn.close();
			}
	        while (true);
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
		new TestCloseListenerConcurrency().run();
    }
}
