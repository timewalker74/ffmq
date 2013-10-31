package net.timewalker.ffmq3.test.local.session;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;

import net.timewalker.ffmq3.test.TestUtils;

/**
 * LocalSessionTest
 */
public class LocalQueueSessionTest extends LocalSessionTest
{    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.local.session.LocalSessionTest#createConnection()
     */
    protected Connection createConnection() throws Exception
    {
        if (isRemote())
            return TestUtils.openRemoteQueueConnection();
        else
            return TestUtils.openLocalQueueConnection();
    }
    
    public void testCreateReceiverQueue() throws Exception
    {        
        QueueSession session;       
        session = ((QueueConnection)connection).createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        
        try
        {
            session.createReceiver(null);
            fail("Should have failed");
        }
        catch (JMSException e)
        {
            assertTrue(e.getMessage().indexOf("destination") != -1);
        }
        
        session.createReceiver(queue1);
        session.close();
    }
    
    public void testCreateReceiverQueueString() throws Exception
    {
        QueueSession session;       
        session = ((QueueConnection)connection).createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        
        try
        {
            session.createReceiver(null,null);
            fail("Should have failed");
        }
        catch (JMSException e)
        {
            assertTrue(e.getMessage().indexOf("destination") != -1);
        }
        
        session.createReceiver(queue1,null);
        session.createReceiver(queue1,"JMSMessageID='toto'");
        session.close();
    }
    
    public void testCreateSenderQueue() throws Exception
    {
        QueueSession session;       
        session = ((QueueConnection)connection).createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        session.createSender(null);
        session.createSender(queue1);
        session.close();
    }
}