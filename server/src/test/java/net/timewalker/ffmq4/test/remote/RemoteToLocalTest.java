/*
 * This file is part of FFMQ.
 *
 * FFMQ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * FFMQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FFMQ; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.timewalker.ffmq4.test.remote;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import net.timewalker.ffmq4.test.AbstractCommTest;
import net.timewalker.ffmq4.test.TestUtils;

/**
 * RemoteToLocalTest
 */
@SuppressWarnings("all")
public class RemoteToLocalTest extends AbstractCommTest
{
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq4.additional.AbstractCommTest#isRemote()
	 */
	@Override
	protected boolean isRemote()
	{
		return true;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq4.additional.AbstractCommTest#isListenerTest()
	 */
	@Override
	protected boolean isListenerTest()
	{
		return false;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq4.additional.AbstractCommTest#isTopicTest()
	 */
	@Override
	protected boolean isTopicTest()
	{
		return false;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq4.additional.AbstractCommTest#useMultipleConnections()
	 */
	@Override
	protected boolean useMultipleConnections()
	{
		return false;
	}
    
	public void testRemoteToLocal() throws Exception
	{
		// Purge queue first
		purgeDestination(queue1);
		
		Connection remoteConn = TestUtils.openRemoteConnection();
		Connection localConn = TestUtils.openLocalConnection();
		
		Session pSession = remoteConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer producer = pSession.createProducer(queue1);
		producer.send(pSession.createTextMessage("foobar"),TestUtils.DELIVERY_MODE,3,0);
		pSession.close();
		
		
		Session cSession = localConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer consumer = cSession.createConsumer(queue1);
		localConn.start();
		TextMessage msg = (TextMessage)consumer.receive(1000);
		assertNotNull(msg);
		assertEquals("foobar", msg.getText());
		cSession.close();
		
		localConn.close();
		remoteConn.close();
	}
	
	public void testLocalToRemote() throws Exception
	{
		// Purge queue first
		purgeDestination(queue1);
		
		Connection remoteConn = TestUtils.openRemoteConnection();
		Connection localConn = TestUtils.openLocalConnection();
		
		Session pSession = localConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer producer = pSession.createProducer(queue1);
		producer.send(pSession.createTextMessage("foobar"),TestUtils.DELIVERY_MODE,3,0);
		pSession.close();
		
		
		Session cSession = remoteConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer consumer = cSession.createConsumer(queue1);
		remoteConn.start();
		TextMessage msg = (TextMessage)consumer.receive(1000);
		assertNotNull(msg);
		assertEquals("foobar", msg.getText());
		cSession.close();
		
		localConn.close();
		remoteConn.close();
	}
	
	private void purgeDestination( Destination destination ) throws JMSException
    {
		Connection connection = TestUtils.openLocalConnection();
        Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(destination);
        connection.start();
        while (consumer.receive(100) != null)
        	continue;
        consumer.close();
        session.commit();
        session.close();
        connection.close();
    }
}
