package net.timewalker.ffmq3.test.utils.queue;

import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.Session;

import net.timewalker.ffmq3.test.utils.factory.DummyMessageFactory;
import net.timewalker.ffmq3.utils.concurrent.SynchronizationPoint;

/**
 * QueueSender
 */
public class QueueSenderThread extends AbstractQueueHandlerThread
{
    private DummyMessageFactory msgFactory;
    private int messageCount;
    private int messageSize;
    private int minDelay;
    private int maxDelay;
    private int deliveryMode;
    private int priority;
    private long timeToLive;
    
    /**
     * Constructor
     */
    public QueueSenderThread(String name,
                             DummyMessageFactory msgFactory,
                             SynchronizationPoint startSynchro,
                             QueueConnection connection,
                             boolean transacted, 
                             int messageCount,
                             int messageSize,
                             int minDelay,
                             int maxDelay,
                             Queue queue,
                             int deliveryMode,
                             int priority,
                             long timeToLive)
    {
        super(name, startSynchro, connection, transacted, Session.AUTO_ACKNOWLEDGE, queue);
        this.msgFactory = msgFactory;
        this.messageCount = messageCount;
        this.messageSize = messageSize;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.deliveryMode = deliveryMode;
        this.priority = priority;
        this.timeToLive = timeToLive;
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.concurrent.SynchronizableThread#run()
     */
    public void run()
    {
        super.run();
        
        try
        {
            QueueSender sender = getSession().createSender(queue);
            
            startSynchro.waitFor();
            
            for (int n = 0 ; n < messageCount ; n++)
            {
                variableWait();
                
                int msgPriority = (priority != -1) ? priority : random.nextInt(10);
                
                Message message = msgFactory.createDummyMessage(messageSize);
                sender.send(message, deliveryMode, msgPriority, timeToLive);
                if (transacted)
                    getSession().commit();
            }
            
            sender.close();
            //System.out.println(getName()+" thread complete ("+messageCount+" messages sent)");
        }
        catch (Throwable e)
        {
            inError = true;
            e.printStackTrace();
        }
    }

    private void variableWait()
    {
        int delta = maxDelay-minDelay;
        long delay = minDelay;

        // Add a random delta
        if (delta > 0)
            delay += random.nextInt(delta+1);

        try
        {
            Thread.sleep(delay);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
