package net.timewalker.ffmq3.test.utils.queue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.Session;

import net.timewalker.ffmq3.test.TestUtils;
import net.timewalker.ffmq3.test.utils.TestRuntimeException;
import net.timewalker.ffmq3.utils.concurrent.SynchronizationPoint;

/**
 * QueueListenerThread
 */
public class QueueListenerThread extends AbstractQueueHandlerThread implements MessageListener
{
    private String messageSelector;
    private volatile int receivedCount;
    
    // Volatile
    private QueueReceiver receiver;
    private boolean stop;
    private int count;
    
    /**
     * Constructor
     */
    public QueueListenerThread(String name,
                               SynchronizationPoint startSynchro,
                               QueueConnection connection,
                               boolean transacted, 
                               int acknowledgeMode,
                               Queue queue,
                               String messageSelector)
    {
        super(name, startSynchro, connection, transacted, acknowledgeMode, queue);
        this.messageSelector = messageSelector;
    }

    /* (non-Javadoc)
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message message)
    {
        try
        {        	
            // Introduce small perturbation on first message
            if (count == 1)
                Thread.sleep(random.nextInt(100));
            
            if (transacted)
            {
            	if (TestUtils.CONSUMER_ROLLBACK_RATE > 0 && 
                    count++ % TestUtils.CONSUMER_ROLLBACK_RATE == 0)
                    {
                        getSession().rollback();
                        return;
                    }
            	
                getSession().commit();
            }
            else
            {
                // Client-side ack
                if (acknowledgeMode == Session.CLIENT_ACKNOWLEDGE)
                {
                	if (TestUtils.CONSUMER_ROLLBACK_RATE > 0 &&
                        	count++ % TestUtils.CONSUMER_ROLLBACK_RATE == 0)
                    {
                        getSession().recover();
                        return;
                    }
                	
                    message.acknowledge();
                }
                else
                {
                	if (TestUtils.CONSUMER_ROLLBACK_RATE > 0 &&
                        count++ % TestUtils.CONSUMER_ROLLBACK_RATE == 0)
                    {
                		throw new TestRuntimeException("Simulated problem");
                    }
                }
            }
            
            receivedCount++;
        }
        catch (TestRuntimeException e)
        {
        	throw e;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.concurrent.SynchronizableThread#run()
     */
    public synchronized void run()
    {
        try
        {
            receiver = getSession().createReceiver(queue,messageSelector);

            receiver.setMessageListener(this);
            notifyStartup();
            
            startSynchro.waitFor();
            
            while (!stop)
            {
               wait();
            }
        }
        catch (Throwable e)
        {
            inError = true;
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.additional.utils.AbstractQueueHandlerThread#close()
     */
    public  void close() throws JMSException
    {
    	synchronized(this)
    	{
	        stop = true;
	        notify();
    	}
        if (receiver != null)
            receiver.close();
        super.close();
    }
    
    /**
     * @return the receivedCount
     */
    public int getReceivedCount()
    {
        return receivedCount;
    }
}
