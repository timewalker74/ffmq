package net.timewalker.ffmq4.test.utils.factory;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * DummyMessageFactory
 */
public interface DummyMessageFactory
{
    /**
     * Create a dummy message of the given size
     */
    public Message createDummyMessage( int size ) throws JMSException;
}
