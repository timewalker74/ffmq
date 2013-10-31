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

package net.timewalker.ffmq4.test.common.message;

import javax.jms.MessageEOFException;

import junit.framework.TestCase;
import net.timewalker.ffmq4.common.message.BytesMessageImpl;

/**
 * BytesMessageImplTest
 */
public class BytesMessageImplTest extends TestCase
{
	public void testWriteNull() throws Exception
	{
		BytesMessageImpl msg;
		
		msg = new BytesMessageImpl();
		
		try { msg.writeBytes(null); } catch (NullPointerException e) { /* OK */ }
		try { msg.writeBytes(null, 0, 0); } catch (NullPointerException e) { /* OK */ }
		try { msg.writeObject(null); } catch (NullPointerException e) { /* OK */ }
		try { msg.writeUTF(null); } catch (NullPointerException e) { /* OK */ }
	}
	
	public void testReset() throws Exception
	{
		BytesMessageImpl msg;
		
		byte[] dummy = { (byte)1 , (byte)2 , (byte)3 };
		
		msg = new BytesMessageImpl();
		msg.writeBytes(dummy);
		msg.reset();
		assertEquals(true,msg.readBoolean());
		assertEquals(true,msg.readBoolean());
		assertEquals(true,msg.readBoolean());
		msg.reset();
		assertEquals(true,msg.readBoolean());
		assertEquals(true,msg.readBoolean());
		assertEquals(true,msg.readBoolean());
	}
	
	public void testClearBody() throws Exception
	{
		BytesMessageImpl msg;
		
		byte[] dummy = { (byte)1 , (byte)2 , (byte)3 };
		
		msg = new BytesMessageImpl();
		msg.writeBytes(dummy);
		msg.reset();
		assertEquals(true,msg.readBoolean());
		assertEquals(true,msg.readBoolean());
		assertEquals(true,msg.readBoolean());
		msg.clearBody();
		msg.writeBytes(dummy);
		msg.reset();
		assertEquals(true,msg.readBoolean());
		assertEquals(true,msg.readBoolean());
		assertEquals(true,msg.readBoolean());
		msg.clearBody();
		msg.writeBytes(dummy);
		msg.clearBody();
		msg.reset();
		try { msg.readByte(); fail("Should have failed"); } catch (MessageEOFException e) { /* OK */ }
	}
	
	public void testRollback() throws Exception
	{
		BytesMessageImpl msg;
		
		byte[] dummy = { (byte)1 , (byte)2 , (byte)3 };
		
		// Boolean
		msg = new BytesMessageImpl();
		msg.writeBytes(dummy);
		msg.reset();
		assertEquals(true,msg.readBoolean());
		
		// Byte
		msg = new BytesMessageImpl();
		msg.writeBytes(dummy);
		msg.reset();
		assertEquals(1,msg.readByte());
		
		// Short
		msg = new BytesMessageImpl();
		msg.writeBytes(dummy);
		msg.reset();
		assertEquals(2+1*256,msg.readShort());
		
		// Char
		msg = new BytesMessageImpl();
		msg.writeBytes(dummy);
		msg.reset();
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// Int
		msg = new BytesMessageImpl();
		msg.writeBytes(dummy);
		msg.reset();
		try { msg.readInt(); fail("Should have failed"); } catch (MessageEOFException e) { /* OK */ }
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// Long
		msg = new BytesMessageImpl();
		msg.writeBytes(dummy);
		msg.reset();
		try { msg.readLong(); fail("Should have failed"); } catch (MessageEOFException e) { /* OK */ }
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// Float
		msg = new BytesMessageImpl();
		msg.writeBytes(dummy);
		msg.reset();
		try { msg.readFloat(); fail("Should have failed"); } catch (MessageEOFException e) { /* OK */ }
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// Double
		msg = new BytesMessageImpl();
		msg.writeBytes(dummy);
		msg.reset();
		try { msg.readDouble(); fail("Should have failed"); } catch (MessageEOFException e) { /* OK */ }
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// String
		msg = new BytesMessageImpl();
		msg.writeBytes(dummy);
		msg.reset();
		try { msg.readUTF(); fail("Should have failed"); } catch (MessageEOFException e) { /* OK */ }
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// Bytes
		msg = new BytesMessageImpl();
		msg.writeBytes(dummy);
		msg.reset();
		byte[] data = new byte[3];
		assertEquals(3,msg.readBytes(data));
		System.out.println(data[0]);
		assertEquals(1, data[0]);
		assertEquals(2, data[1]);
		assertEquals(3, data[2]);
		
		assertEquals(3, msg.getBodyLength());
	}
}
