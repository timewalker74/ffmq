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
import java.util.Random;

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

import net.timewalker.ffmq3.FFMQConstants;
import net.timewalker.ffmq3.test.utils.factory.MessageCreator;

/**
 * TestQueueFull
 */
public class TestQueueFull
{
	public static void main(String[] args) throws Exception
    {
//        Properties testSettings = new Properties();
//        FileInputStream in = new FileInputStream("../conf/ffmq-server.properties");
//        testSettings.load(in);
//        in.close();
//        
//        PropertyConfigurator.configure(testSettings);
//
//        Settings settings = new Settings(testSettings);
//
//        FFMQEngine engine = new FFMQEngine(TestUtils.LOCAL_ENGINE_NAME,settings,null);
//        engine.deploy();
//        
//        TcpListener listener = new TcpListener(engine,
//                                               FFMQConstants.DEFAULT_SERVER_HOST,
//                                               FFMQConstants.DEFAULT_SERVER_PORT,
//                                               settings);
//        listener.start();
        //---------------------------------------------------------------------
        testQueueFull("tcp://"+FFMQConstants.DEFAULT_SERVER_HOST+":"+FFMQConstants.DEFAULT_SERVER_PORT);

        //---------------------------------------------------------------------
        
//        listener.stop();
//        engine.undeploy(); 
    }
	
	private static void testQueueFull( String providerURL )
    {
		Random rand = new Random();
		
		try
		{
	        Hashtable env = new Hashtable();
	        env.put(Context.INITIAL_CONTEXT_FACTORY, FFMQConstants.JNDI_CONTEXT_FACTORY);
	        env.put(Context.PROVIDER_URL, providerURL);
	        Context context1 = new InitialContext(env);
	        
	        ConnectionFactory connFactory1 = (ConnectionFactory)context1.lookup(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME);
	      
	        Connection conn1 = connFactory1.createConnection();
	        conn1.start();
	        
	        Session session1 = conn1.createSession(false,Session.AUTO_ACKNOWLEDGE);
	
	        Queue queue1 = session1.createQueue("TEST1");
	
	        System.out.println("Filling queue ...");
	        
	        MessageProducer producer = session1.createProducer(queue1);
	        int count = 0;
	        try
	        {
		        while (true)
		        {
			        Message message = MessageCreator.createTextMessage(2050);
			        
			        producer.send(message,DeliveryMode.NON_PERSISTENT,rand.nextInt(10),0);
			        //session1.commit();
			        
			        count++;
			        if ((count % 1000) == 0)
			        	System.out.println("Produced : "+count);
			        
//			        if (count > 10)
//			        	break;
		        }
	        }
	        catch (JMSException e)
	        {
	        	//session1.rollback();
	        	e.printStackTrace();
	        }
	        System.out.println("Added "+count+" messages");
	        
	        
	        //------------------------------------------------------------------------------
	        
	        
	        System.out.println("Reading messages back ...");
	        
	        MessageConsumer consumer = session1.createConsumer(queue1);
	        
	        count = 0;
	        long last = System.currentTimeMillis();
	        Message msg;
	        while ((msg = consumer.receiveNoWait()) != null)
	        {
	        	count ++;
	        	if ((count % 1000) == 0)
	        	{
	        		long now = System.currentTimeMillis();
		        	System.out.println("Consumed : "+count+" ("+(now-last)+" ms)");
		        	last = now;
	        	}
	        	
	        	//System.out.println(msg);
	        	msg.getJMSMessageID();
//	        	if (count > 5)
//	        		break;
	        }
	        System.out.println("Removed "+count+" messages");
	        consumer.close();
	     
	        System.out.println("Closing ...");
	        
	        session1.close();
	        conn1.close();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
    }
}
