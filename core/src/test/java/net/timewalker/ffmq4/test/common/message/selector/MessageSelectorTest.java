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

package net.timewalker.ffmq4.test.common.message.selector;

import javax.jms.Message;

import net.timewalker.ffmq4.common.message.MessageSelector;
import net.timewalker.ffmq4.common.message.TextMessageImpl;
import junit.framework.TestCase;

/**
 * MessageSelectorTest
 */
public class MessageSelectorTest extends TestCase
{
	public void testEval() throws Exception
	{
		MessageSelector selector = new MessageSelector("lbId=1");
		
		Message msg = new TextMessageImpl();
    	msg.setByteProperty("lbId", (byte)1);
    	
    	for (int n = 0; n < 20000; n++)
		{
    		assertTrue(selector.matches(msg));	
		}
		
	}
}
