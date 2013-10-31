package net.timewalker.ffmq3.test.utils.topic;

import java.util.Random;

import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;

import net.timewalker.ffmq3.test.utils.factory.DummyMessageFactory;
import net.timewalker.ffmq3.utils.concurrent.SynchronizationPoint;

/**
 * TopicPublisherThread
 */
public class TopicPublisherThread extends AbstractTopicHandlerThread
{
    private DummyMessageFactory msgFactory;
    private int messageCount;
    private int messageSize;
    private int minDelay;
    private int maxDelay;
    private int deliveryMode;
    private int priority;
    private long timeToLive;
    
    private Random random = new Random();

    private SynchronizationPoint startSynchro;
    
    /**
     * Constructor
     */
    public TopicPublisherThread(String name,
                                DummyMessageFactory msgFactory,
                                SynchronizationPoint startSynchro,
                                TopicConnection connection,
                                boolean transacted, 
                                int messageCount,
                                int messageSize,
                                int minDelay,
                                int maxDelay,
                                Topic topic,
                                int deliveryMode,
                                int priority,
                                long timeToLive)
    {
        super(name, startSynchro, connection, transacted, Session.AUTO_ACKNOWLEDGE, topic);
        this.msgFactory = msgFactory;
        this.messageCount = messageCount;
        this.messageSize = messageSize;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.deliveryMode = deliveryMode;
        this.priority = priority;
        this.timeToLive = timeToLive;
        this.startSynchro = startSynchro;
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.concurrent.SynchronizableThread#run()
     */
    @Override
	public void run()
    {
        try
        {
            TopicPublisher publisher = getSession().createPublisher(topic);
            notifyStartup();
            
            startSynchro.waitFor();
            
            for (int n = 0 ; n < messageCount ; n++)
            {
                variableWait();
                
                int msgPriority = (priority != -1) ? priority : random.nextInt(10);
                
                Message message = msgFactory.createDummyMessage(messageSize);
                publisher.publish(message, deliveryMode, msgPriority, timeToLive);
                if (transacted)
                    getSession().commit();
            }
            
            publisher.close();
            //System.out.println(getName()+" thread complete ("+messageCount+" messages sent)");
        }
        catch (Throwable e)
        {
            inError = true;
            e.printStackTrace();
        }
    }

//    private Message createMessage( int index ) throws JMSException
//    {
//        TextMessage msg = getSession().createTextMessage();
//        
//        StringBuffer text = new StringBuffer(messageSize+20);
//        text.append(getName());
//        text.append("-");
//        text.append(index);
//        text.append("-");
//        for (int n = 0 ; n < messageSize ; n++)
//            text.append("X");       
//        
//        msg.setText(text.toString());
//        
//        return msg;
//    }
    
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
