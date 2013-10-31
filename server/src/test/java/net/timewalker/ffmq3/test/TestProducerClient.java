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
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import net.timewalker.ffmq3.FFMQConstants;
import net.timewalker.ffmq3.storage.data.DataStoreFullException;

/**
 * TestProducerClient
 */
public class TestProducerClient implements Runnable,ExceptionListener
{
	private static final boolean USE_QUEUE = false;
	private static final int DELIVERY_MODE = DeliveryMode.NON_PERSISTENT;
	private static final int AMOUNT = 500000;
	
	private boolean stopRequired;
	private Connection conn;
	private int sent = 0;
	private int commited = 0;
	private int commitRate = 1;
	private long sendDelay = 0;
	private long msgTTL = 0; //5*1000;
	
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
	        
	        conn = connFactory.createConnection("test","test");
	        conn.setExceptionListener(this);
	        conn.start();
	        
	        final Session session = conn.createSession(true,Session.AUTO_ACKNOWLEDGE);
	        
	        Destination destination = USE_QUEUE ? (Destination)session.createQueue("TEST") : (Destination)session.createTopic("VOLATILE");
	        
	        MessageProducer producer = session.createProducer(destination);
	        producer.setDeliveryMode(DELIVERY_MODE);
	        
	        if (msgTTL > 0)
	            producer.setTimeToLive(msgTTL);
	        
//	        new Thread(){
//	        	public void run() {
//	        		try
//	        		{
//	        			Thread.sleep(3000);
//	        			session.close();
//	        		}
//	        		catch (Exception e)
//	        		{
//	        			e.printStackTrace();
//	        		}
//	        	}
//	        }.start();
	        
	        System.out.println("---------------------------------------------------------------------");
	        int count = 0;
	        try
	        {
		        while (!stopRequired && count  < AMOUNT)
		        {
		        	Message msg = session.createTextMessage("MSG_"+count++);
		        	
		        	//System.out.println("Sending : "+msg);
		        	producer.send(msg);
		        	sent++;
		        	
		        	if (count%commitRate == 0)
		        	{	
		        		session.commit();
		        		commited += commitRate;
		        		
		        		System.out.print(".");
		        		
		        		if (sendDelay > 0)
		        		    wait(sendDelay);
		        	}
		        }
		        
		        if (count%commitRate != 0)
		        {
		            session.commit();
		            commited = count;
		        }
	        }
	        finally
	        {
		        producer.close();
		        session.close();
		        conn.close();
	        }
		}
		catch (DataStoreFullException e)
		{
			System.err.println("Producer client failed : "+e);
		}
		catch (Throwable e)
		{
			System.err.println("Producer client failed");
			e.printStackTrace();
		}
		finally
		{
			System.out.println("---------------------------------------------------------------------");
	        System.out.println("Sent     = "+sent);
	        System.out.println("Commited = "+commited);
		}
	}
	
	public static void main(String[] args) throws Exception
    {
		new TestProducerClient().run();
    }
}
