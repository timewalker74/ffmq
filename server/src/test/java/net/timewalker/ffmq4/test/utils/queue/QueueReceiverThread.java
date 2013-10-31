package net.timewalker.ffmq4.test.utils.queue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.Session;

import net.timewalker.ffmq4.test.TestUtils;
import net.timewalker.ffmq4.utils.concurrent.SynchronizationPoint;

/**
 * QueueReceiver
 */
@SuppressWarnings("all")
public class QueueReceiverThread extends AbstractQueueHandlerThread
{
    private String messageSelector;
    private volatile int receivedCount;
    
    // Volatile
    private QueueReceiver receiver;
    private boolean stop;
    
    /**
     * Constructor
     */
    public QueueReceiverThread(String name,
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
     * @see net.timewalker.ffmq4.utils.concurrent.SynchronizableThread#run()
     */
    public void run()
    {
        int count = 0;
        try
        {
            receiver = getSession().createReceiver(queue,messageSelector);
            
            notifyStartup();
            
            startSynchro.waitFor();
            
            // Introduce small perturbation on first message
            Thread.sleep(random.nextInt(100));
            
            Message msg;
            while (!stop)
            {
                if ((msg = receiver.receive()) == null)
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
