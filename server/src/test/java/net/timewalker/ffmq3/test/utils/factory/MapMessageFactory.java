package net.timewalker.ffmq3.test.utils.factory;

import javax.jms.JMSException;
import javax.jms.Message;


/**
 * MapMessageFactory
 */
public class MapMessageFactory implements DummyMessageFactory
{
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.additional.utils.DummyMessageFactory#createDummyMessage()
     */
    public Message createDummyMessage( int size ) throws JMSException
    {
        return MessageCreator.createMapMessage(size);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "MapMessage";
    }
}
