package net.timewalker.ffmq3.test.utils.topic;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSubscriber;

import net.timewalker.ffmq3.test.TestUtils;
import net.timewalker.ffmq3.test.utils.TestRuntimeException;
import net.timewalker.ffmq3.utils.concurrent.SynchronizationPoint;

/**
 * TopicListenerThread
 */
@SuppressWarnings("all")
public class TopicListenerThread extends AbstractTopicHandlerThread implements MessageListener
{
    private String messageSelector;
    private boolean noLocal;
    private volatile int receivedCount;
    
    // Volatile
    private TopicSubscriber subscriber;
    private boolean stop;
    private int count;
    
    /**
     * Constructor
     */
    public TopicListenerThread(String name,
    		                   SynchronizationPoint startSynchro ,
                               TopicConnection connection,
                               boolean transacted, 
                               int acknowledgeMode,
                               Topic topic,
                               String messageSelector,
                               boolean noLocal)
    {
        super(name, startSynchro, connection, transacted, acknowledgeMode, topic);
        this.messageSelector = messageSelector;
        this.noLocal = noLocal;
    }

    /* (non-Javadoc)
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message message)
    {
        try
        {
        	// Introduce small perturbation on first message*
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
            subscriber = getSession().createSubscriber(topic,messageSelector,noLocal);

            subscriber.setMessageListener(this);
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
    public void close() throws JMSException
    {
    	synchronized (this) 
    	{
    		stop = true;
            notify();
		}
        
        if (subscriber != null)
            subscriber.close();
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
