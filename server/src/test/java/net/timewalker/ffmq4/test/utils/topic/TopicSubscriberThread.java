package net.timewalker.ffmq4.test.utils.topic;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSubscriber;

import net.timewalker.ffmq4.test.TestUtils;
import net.timewalker.ffmq4.utils.concurrent.SynchronizationPoint;

/**
 * TopicSubscriberThread
 */
@SuppressWarnings("all")
public class TopicSubscriberThread extends AbstractTopicHandlerThread
{
    private String messageSelector;
    private boolean noLocal;
    private volatile int receivedCount = 0;
    
    // Volatile
    private TopicSubscriber subscriber;
    private boolean stop;
    
    /**
     * Constructor
     */
    public TopicSubscriberThread(String name,
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
     * @see net.timewalker.ffmq4.utils.concurrent.SynchronizableThread#run()
     */
    public void run()
    {
        int count = 0;
        try
        {
            subscriber = getSession().createSubscriber(topic,messageSelector,noLocal);
            notifyStartup();

            startSynchro.waitFor();
            
            // Introduce small perturbation on first message
            Thread.sleep(random.nextInt(100));
            
            Message msg;
            while (!stop)
            {
                if ((msg = subscriber.receive()) == null)
                {
                    if (!stop)
                        throw new IllegalStateException("Receiver was interrupted");
                        
                    break;
                }
                
                if (transacted)
                {
                    if (TestUtils.CONSUMER_ROLLBACK_RATE > 0 &&
                    	count++ % TestUtils.CONSUMER_ROLLBACK_RATE == 0)
                    {
                        getSession().rollback();
                        continue;
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
                            continue;
                        }

                        msg.acknowledge();
                    }
                }
                
                receivedCount++;
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
     * @see net.timewalker.ffmq4.additional.utils.AbstractQueueHandlerThread#close()
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

    /*
     * (non-Javadoc)
     * @see java.lang.Thread#toString()
     */
	public String toString() 
	{
		return super.toString()+" - "+subscriber;
	}
}
