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

package net.timewalker.ffmq3.test.jndi;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.Context;

import junit.framework.TestCase;
import net.timewalker.ffmq3.FFMQConstants;
import net.timewalker.ffmq3.utils.JNDITools;

/**
 * JndiTest
 */
public class JndiTest extends TestCase
{
	public void testContextFactory() throws Exception
	{
		Context ctx = JNDITools.getContext(FFMQConstants.JNDI_CONTEXT_FACTORY, "foobar", null);
		assertNotNull(ctx);
	}
	
	public void testConnectionFactory() throws Exception
	{
		Context ctx = JNDITools.getContext(FFMQConstants.JNDI_CONTEXT_FACTORY, "foobar", null);
		assertNotNull(ctx);
		
		ConnectionFactory connFactory = (ConnectionFactory)ctx.lookup(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME);
		assertNotNull(connFactory);
		
		try
		{
			connFactory.createConnection();
			fail("Should have failed");
		}
		catch (JMSException e)
		{
			assertTrue(e.getMessage().indexOf("foobar") != -1);
		}
	}
	
	public void testDestinationLookup() throws Exception
	{
		Context ctx = JNDITools.getContext(FFMQConstants.JNDI_CONTEXT_FACTORY, "foobar", null);
		assertNotNull(ctx);
		
		Queue queue = (Queue)ctx.lookup("queue/TEST");
		assertNotNull(queue);
		assertEquals("TEST", queue.getQueueName());
		
		Topic topic = (Topic)ctx.lookup("topic/TEST");
		assertNotNull(topic);
		assertEquals("TEST", topic.getTopicName());
	}
}
