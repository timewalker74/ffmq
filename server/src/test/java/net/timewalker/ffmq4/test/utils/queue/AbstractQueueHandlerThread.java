package net.timewalker.ffmq4.test.utils.queue;

import java.util.Random;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;

import net.timewalker.ffmq4.utils.concurrent.SynchronizableThread;
import net.timewalker.ffmq4.utils.concurrent.SynchronizationPoint;

/**
 * AbstractQueueHandler
 */
public abstract class AbstractQueueHandlerThread extends SynchronizableThread
{
    // Setup
    private QueueConnection connection;
    protected Queue queue;
    protected boolean transacted;
    protected int acknowledgeMode;
    
    // Volatile
    private QueueSession session;
    protected boolean inError;
    
    // Runtime
    protected SynchronizationPoint startSynchro;
    protected static final Random random = new Random();
    
    /**
     * Constructor
     */
    public AbstractQueueHandlerThread( String name , SynchronizationPoint startSynchro , QueueConnection connection , boolean transacted , int acknowledgeMode , Queue queue )
    {
        super(name);
        this.startSynchro = startSynchro;
        this.connection = connection;
        this.transacted = transacted;
        this.acknowledgeMode = acknowledgeMode;
        this.queue = queue;
    }
    
    /**
     * @return the inError
     */
    public boolean isInError()
    {
        return inError;
    }
    
    protected QueueSession getSession() throws JMSException
    {
        if (session == null)
            session = connection.createQueueSession(transacted, acknowledgeMode);
        return session;
    }
    
    public void close() throws JMSException
    {
        if (session != null)
            session.close();
    }
}
