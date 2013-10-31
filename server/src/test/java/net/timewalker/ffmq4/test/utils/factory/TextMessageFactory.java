package net.timewalker.ffmq3.test.utils.factory;

import javax.jms.JMSException;
import javax.jms.Message;


/**
 * TextMessageFactory
 */
public class TextMessageFactory implements DummyMessageFactory
{
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.additional.utils.DummyMessageFactory#createDummyMessage()
     */
    @Override
	public Message createDummyMessage( int size ) throws JMSException
    {
        return MessageCreator.createTextMessage(size);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
        return "TextMessage";
    }
}
