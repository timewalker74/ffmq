package net.timewalker.ffmq4.test;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import net.timewalker.ffmq4.FFMQConstants;

public class TestCreateConsumerSpeed implements Runnable, ExceptionListener
{
    private static final String PROVIDER_URL = "tcpnio://localhost:"+TestUtils.TEST_SERVER_PORT;
    
    private static final boolean USE_QUEUE = false;
    
    private Connection conn;
    
    /* (non-Javadoc)
     * @see javax.jms.ExceptionListener#onException(javax.jms.JMSException)
     */
    @Override
	public void onException(JMSException e)
    {
        e.printStackTrace();
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
            
            conn = connFactory.createConnection();
            conn.setExceptionListener(this);
            conn.start();
            
            Session session = conn.createSession(true,Session.SESSION_TRANSACTED);
            Destination destination = USE_QUEUE ? (Destination)session.createQueue("TEST") : (Destination)session.createTopic("VOLATILE");
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0 ; i < 10000 ; i++)
            {
                MessageConsumer consumer = session.createConsumer(destination);
                consumer.close();   
            }
            
            long endTime = System.currentTimeMillis();
            
            System.out.println((endTime-startTime)+" ms");
            
            session.close();
            conn.close();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        finally
        {
            System.out.println("---------------------------------------------------------------------");
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        new TestCreateConsumerSpeed().run();
    }
}
