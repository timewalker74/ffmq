package net.timewalker.ffmq4.test.utils.topic;

import java.util.Random;

import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

import net.timewalker.ffmq4.utils.concurrent.SynchronizableThread;
import net.timewalker.ffmq4.utils.concurrent.SynchronizationPoint;

/**
 * AbstractTopicHandlerThread
 */
public class AbstractTopicHandlerThread extends SynchronizableThread
{
    // Setup
    private TopicConnection connection;
    protected Topic topic;
    protected boolean transacted;
    protected int acknowledgeMode;
    
    // Volatile
    private TopicSession session;
    protected boolean inError;
    
    // Runtime
    protected SynchronizationPoint startSynchro;
    protected static final Random random = new Random();
    
    /**
     * Constructor
     */
    public AbstractTopicHandlerThread( String name , 
    		                           SynchronizationPoint startSynchro ,
                                       TopicConnection connection , 
                                       boolean transacted , 
                                       int acknowledgeMode ,
                                       Topic topic )
    {
        super(name);
        this.startSynchro = startSynchro;
        this.connection = connection;
        this.transacted = transacted;
        this.acknowledgeMode = acknowledgeMode;
        this.topic = topic;
    }
    
    /**
     * @return the inError
     */
    public boolean isInError()
    {
        return inError;
    }
    
    protected TopicSession getSession() throws JMSException
    {
        if (session == null)
            session = connection.createTopicSession(transacted, acknowledgeMode);
        return session;
    }
    
    public synchronized void close() throws JMSException
    {
        if (session != null)
            session.close();
    }
}
