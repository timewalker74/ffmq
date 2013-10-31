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
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import net.timewalker.ffmq3.FFMQConstants;

/**
 * TestDurableSubscription
 */
public class TestDurableSubscription
{
    public static void main(String[] args) throws Exception
    {
//        Properties testSettings = new Properties();
//        FileInputStream in = new FileInputStream("../conf/ffmq-server.properties");
//        testSettings.load(in);
//        in.close();
//        
//        PropertyConfigurator.configure(testSettings);

//        Settings settings = new Settings(testSettings);
//        LocalEngineSetup setup = new LocalEngineSetup(settings);
//
//        LocalEngine engine = new LocalEngine("testengine");
//        engine.deploy(setup);
//        
//        TcpListener server = new TcpListener(engine,
//                                                       FFMQConstants.DEFAULT_SERVER_HOST,
//                                                       FFMQConstants.DEFAULT_SERVER_PORT,
//                                                       settings);
//        
//        // Start the server thread
//        SynchronizableThread serverThread = new SynchronizableThread(server,"NetworkServer");
//        serverThread.start();
//        serverThread.waitForStartup();
        
        //---------------------------------------------------------------------
        //testDurableSubscriber("vm://testengine");
        testDurableSubscriber("tcp://"+FFMQConstants.DEFAULT_SERVER_HOST+":"+FFMQConstants.DEFAULT_SERVER_PORT);
        //---------------------------------------------------------------------
        
//        server.pleaseStop();
//        engine.undeploy();
        
    }
    
    private static void testDurableSubscriber( String providerURL ) throws Exception
    {
        Hashtable<String,Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, FFMQConstants.JNDI_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, providerURL);
        env.put(FFMQConstants.JNDI_ENV_CLIENT_ID, "testclient");
        Context context1 = new InitialContext(env);
        
        Hashtable<String,Object> env2 = new Hashtable<>();
        env2.put(Context.INITIAL_CONTEXT_FACTORY, FFMQConstants.JNDI_CONTEXT_FACTORY);
        //env2.put(Context.PROVIDER_URL, "vm://testengine");
        env.put(Context.PROVIDER_URL, providerURL);
        Context context2 = new InitialContext(env2);
        
        ConnectionFactory connFactory1 = (ConnectionFactory)context1.lookup(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME);
        ConnectionFactory connFactory2 = (ConnectionFactory)context2.lookup(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME);
        
        Connection conn1 = connFactory1.createConnection();
        Connection conn2 = connFactory2.createConnection();
        conn1.start();
        conn2.start();
        
        Session session1 = conn1.createSession(true,Session.AUTO_ACKNOWLEDGE);
        Session session2 = conn2.createSession(true,Session.AUTO_ACKNOWLEDGE);
        
        Topic topic1 = session1.createTopic("durabletopic1");
        Topic topic2 = session2.createTopic("durabletopic1");
        
        MessageConsumer consumer = session1.createDurableSubscriber(topic1, "sub1");
        
        MessageProducer producer = session2.createProducer(topic2);
        Message message = session2.createTextMessage("foobar");
        producer.send(message);
        session2.commit();
        
        System.out.println("---------------------------------------------------------------------");
        Message msg;
        while ((msg = consumer.receiveNoWait()) != null)
            System.out.println(msg);
        session1.commit();
        consumer.close();
       
        message = session2.createTextMessage("foobar2");
        producer.send(message);
        session2.commit();
        producer.close();
        
        consumer = session1.createDurableSubscriber(topic1, "sub1");
        System.out.println("---------------------------------------------------------------------");
        while ((msg = consumer.receiveNoWait()) != null)
            System.out.println(msg);
        session1.commit();
        consumer.close();
        
        session1.unsubscribe("sub1");
        
        session1.close();
        session2.close();
        conn1.close();
        conn2.close();
    }
}
