package net.timewalker.ffmq4.test.utils;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import net.timewalker.ffmq4.utils.concurrent.SynchronizableThread;
import net.timewalker.ffmq4.utils.concurrent.SynchronizationPoint;

/**
 * AbstractHandlerThread
 */
public class AbstractHandlerThread extends SynchronizableThread
{
    // Setup
    private Connection connection;
    protected Destination destination;
    protected boolean transacted;
    protected int acknowledgeMode;
    
    // Volatile
    private Session session;
    protected boolean inError;
    
    // Runtime
    protected SynchronizationPoint startSynchro;
    
    /**
     * Constructor
     */
    public AbstractHandlerThread( String name , SynchronizationPoint startSynchro , Connection connection , boolean transacted , int acknowledgeMode , Destination destination )
    {
        super(name);
        this.startSynchro = startSynchro;
        this.connection = connection;
        this.transacted = transacted;
        this.acknowledgeMode = acknowledgeMode;
        this.destination = destination;
    }
    
    /**
     * @return the inError
     */
    public boolean isInError()
    {
        return inError;
    }
    
    protected Session getSession() throws JMSException
    {
        if (session == null)
            session = connection.createSession(transacted, acknowledgeMode);
        return session;
    }
    
    public synchronized void close() throws JMSException
    {
        if (session != null)
            session.close();
    }
}
