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

package net.timewalker.ffmq4.test.local;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.timewalker.ffmq4.local.TransactionItem;
import net.timewalker.ffmq4.local.TransactionSet;

/**
 * TransactionSetTest
 */
public class TransactionSetTest extends TestCase
{
	public void testAdd() throws Exception
	{
		TransactionSet set = new TransactionSet();
		
		set.add(1,"msg1",1,null);
		set.add(2,"msg2",1,null);
		set.add(3,"msg3",1,null);
		set.add(4,"msg4",1,null);
		
		assertEquals(4,set.size());
		
		TransactionItem[] items = set.clear();
		assertEquals(4,items.length);
		for(int n=0;n<items.length;n++)
			assertEquals(n+1,items[n].getHandle());
		
		assertEquals(0,set.size());
	}
	
	public void testAutoExtend() throws Exception
	{
		TransactionSet set = new TransactionSet();
		
		for (int i = 1; i <= 100; i++)
			set.add(i,"msg"+i,1,null);
		
		assertEquals(100,set.size());
		
		TransactionItem[] items = set.clear();
		assertEquals(100,items.length);
		for(int n=0;n<items.length;n++)
			assertEquals(n+1,items[n].getHandle());
		
		assertEquals(0,set.size());
	}
	
	public void testClear() throws Exception
	{
		TransactionSet set = new TransactionSet();
		
		set.add(1,"msg1",1,null);
		set.add(2,"msg2",1,null);
		set.add(3,"msg3",1,null);
		set.add(4,"msg4",1,null);
		
		assertEquals(4,set.size());
		
		TransactionItem[] items = set.clear();
		assertEquals(4,items.length);
		for(int n=0;n<items.length;n++)
			assertEquals(n+1,items[n].getHandle());
		
		assertEquals(0,set.size());
		
		set.add(1,"msg1",1,null);
		set.add(2,"msg2",1,null);
		set.add(3,"msg3",1,null);
		
		assertEquals(3,set.size());
		
		items = set.clear();
		assertEquals(3,items.length);
		for(int n=0;n<items.length;n++)
			assertEquals(n+1,items[n].getHandle());
		
		assertEquals(0,set.size());
	}
	
	public void testPartialClear() throws Exception
	{
		TransactionSet set = new TransactionSet();
		List<String> deliveredMessageIDs = new ArrayList<>();
		
		int msgCount = 50;
		
		for (int n = 1; n <= msgCount; n++)
		{
			for(int i=1;i<=msgCount;i++)
				set.add(i,"msg"+i,1,null);
		
			assertEquals(msgCount,set.size());
		
			deliveredMessageIDs.clear();
			deliveredMessageIDs.add("msg"+n);
			TransactionItem[] items = set.clear(deliveredMessageIDs);
	
			assertEquals(msgCount-1,set.size());
			assertEquals(1, items.length);
			assertEquals(n, items[0].getHandle());
			
			set.clear();
		}
	}
}
