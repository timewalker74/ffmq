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

package net.timewalker.ffmq3.test.common.message.selector;

import java.util.List;

import javax.jms.Message;

import junit.framework.TestCase;
import net.timewalker.ffmq3.common.message.MessageSelector;
import net.timewalker.ffmq3.common.message.TextMessageImpl;
import net.timewalker.ffmq3.common.message.selector.SelectorIndexKey;

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
	
	public void testIndexing() throws Exception
	{
		check("",(String[])null);
		check("t>2",(String[])null);
		check("t is not null",(String[])null);
		check("t=1 or x=2",(String[])null);
		
		check("t=1",new String[] { "t" });
		check("t=1 and x=2",new String[] {"t","x"});
		check("t>2 and a=b and t=1 and x=2",new String[] {"t","x"});
		check("t=1 and (x=2 or b=3)",new String[] {"t"});
	}
	
	private void check( String expr , String[] keys ) throws Exception
	{
		List indexKeys = new MessageSelector(expr).getIndexableKeys();
		if (keys == null)
			assertNull(indexKeys);
		else
		{
			assertNotNull(indexKeys);
			assertEquals(keys.length, indexKeys.size());
			
			for(int n=0;n<indexKeys.size();n++)
			{
			    SelectorIndexKey indexKey = (SelectorIndexKey)indexKeys.get(n);
				if (!contains(keys,indexKey.getHeaderName()))
					fail("Unexpected : "+indexKey.getHeaderName());
			}
		}
	}
	
	private boolean contains(String[] values,String value)
	{
		for(int n=0;n<values.length;n++)
		{
			String v = values[n];
			if (v.equals(value))
				return true;
		}
		return false;
	}
}
