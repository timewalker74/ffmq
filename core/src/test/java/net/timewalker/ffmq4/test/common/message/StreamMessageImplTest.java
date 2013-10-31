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

package net.timewalker.ffmq3.test.common.message;

import javax.jms.MessageFormatException;

import junit.framework.TestCase;
import net.timewalker.ffmq3.common.message.StreamMessageImpl;

/**
 * StreamMessageImplTest
 */
public class StreamMessageImplTest extends TestCase
{
	public void testBooleanConversion() throws Exception
	{
		StreamMessageImpl msg;
		
		// Boolean
		msg = new StreamMessageImpl();
		msg.writeBoolean(true);
		msg.markAsReadOnly();
		assertTrue(msg.readBoolean());
		
		// Byte
		msg = new StreamMessageImpl();
		msg.writeBoolean(true);
		msg.markAsReadOnly();
		try { msg.readByte(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertTrue(msg.readBoolean());
		
		// Short
		msg = new StreamMessageImpl();
		msg.writeBoolean(true);
		msg.markAsReadOnly();
		try { msg.readShort(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertTrue(msg.readBoolean());

		// Char
		msg = new StreamMessageImpl();
		msg.writeBoolean(true);
		msg.markAsReadOnly();
		try { msg.readChar(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertTrue(msg.readBoolean());
		
		// Int
		msg = new StreamMessageImpl();
		msg.writeBoolean(true);
		msg.markAsReadOnly();
		try { msg.readInt(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertTrue(msg.readBoolean());
		
		// Long
		msg = new StreamMessageImpl();
		msg.writeBoolean(true);
		msg.markAsReadOnly();
		try { msg.readLong(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertTrue(msg.readBoolean());
		
		// Float
		msg = new StreamMessageImpl();
		msg.writeBoolean(true);
		msg.markAsReadOnly();
		try { msg.readFloat(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertTrue(msg.readBoolean());
		
		// Double
		msg = new StreamMessageImpl();
		msg.writeBoolean(true);
		msg.markAsReadOnly();
		try { msg.readDouble(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertTrue(msg.readBoolean());
		
		// String
		msg = new StreamMessageImpl();
		msg.writeBoolean(true);
		msg.markAsReadOnly();
		assertEquals("true",msg.readString());
		
		// Bytes
		msg = new StreamMessageImpl();
		msg.writeBoolean(true);
		msg.markAsReadOnly();
		try { msg.readBytes(new byte[1]); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertTrue(msg.readBoolean());
	}

	public void testByteConversion() throws Exception
	{
		StreamMessageImpl msg;
		
		// Boolean
		msg = new StreamMessageImpl();
		msg.writeByte((byte)123);
		msg.markAsReadOnly();
		try { msg.readBoolean(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readByte());
		
		// Byte
		msg = new StreamMessageImpl();
		msg.writeByte((byte)123);
		msg.markAsReadOnly();
		assertEquals(123,msg.readByte());
		
		// Short
		msg = new StreamMessageImpl();
		msg.writeByte((byte)123);
		msg.markAsReadOnly();
		assertEquals(123,msg.readShort());
		
		// Char
		msg = new StreamMessageImpl();
		msg.writeByte((byte)123);
		msg.markAsReadOnly();
		try { msg.readChar(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readByte());
		
		// Int
		msg = new StreamMessageImpl();
		msg.writeByte((byte)123);
		msg.markAsReadOnly();
		assertEquals(123,msg.readInt());
		
		// Long
		msg = new StreamMessageImpl();
		msg.writeByte((byte)123);
		msg.markAsReadOnly();
		assertEquals(123,msg.readLong());
		
		// Float
		msg = new StreamMessageImpl();
		msg.writeByte((byte)123);
		msg.markAsReadOnly();
		try { msg.readFloat(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.readByte());
		
		// Double
		msg = new StreamMessageImpl();
		msg.writeByte((byte)123);
		msg.markAsReadOnly();
		try { msg.readDouble(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.readByte());
		
		// String
		msg = new StreamMessageImpl();
		msg.writeByte((byte)123);
		msg.markAsReadOnly();
		assertEquals("123",msg.readString());
		
		// Bytes
		msg = new StreamMessageImpl();
		msg.writeByte((byte)123);
		msg.markAsReadOnly();
		try { msg.readBytes(new byte[1]); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readByte());
	}
	
	public void testShortConversion() throws Exception
	{
		StreamMessageImpl msg;
		
		// Boolean
		msg = new StreamMessageImpl();
		msg.writeShort((short)123);
		msg.markAsReadOnly();
		try { msg.readBoolean(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readShort());
		
		// Byte
		msg = new StreamMessageImpl();
		msg.writeShort((short)123);
		msg.markAsReadOnly();
		try { msg.readByte(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readShort());
		
		// Short
		msg = new StreamMessageImpl();
		msg.writeShort((short)123);
		msg.markAsReadOnly();
		assertEquals(123,msg.readShort());
		
		// Char
		msg = new StreamMessageImpl();
		msg.writeShort((short)123);
		msg.markAsReadOnly();
		try { msg.readChar(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readShort());
		
		// Int
		msg = new StreamMessageImpl();
		msg.writeShort((short)123);
		msg.markAsReadOnly();
		assertEquals(123,msg.readInt());
		
		// Long
		msg = new StreamMessageImpl();
		msg.writeShort((short)123);
		msg.markAsReadOnly();
		assertEquals(123,msg.readLong());
		
		// Float
		msg = new StreamMessageImpl();
		msg.writeShort((short)123);
		msg.markAsReadOnly();
		try { msg.readFloat(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.readShort());
		
		// Double
		msg = new StreamMessageImpl();
		msg.writeShort((short)123);
		msg.markAsReadOnly();
		try { msg.readDouble(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.readShort());
		
		// String
		msg = new StreamMessageImpl();
		msg.writeShort((short)123);
		msg.markAsReadOnly();
		assertEquals("123",msg.readString());
		
		// Bytes
		msg = new StreamMessageImpl();
		msg.writeShort((short)123);
		msg.markAsReadOnly();
		try { msg.readBytes(new byte[1]); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readShort());
	}
	
	public void testCharConversion() throws Exception
	{
		StreamMessageImpl msg;
		
		// Boolean
		msg = new StreamMessageImpl();
		msg.writeChar('c');
		msg.markAsReadOnly();
		try { msg.readBoolean(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals('c',msg.readChar());
		
		// Byte
		msg = new StreamMessageImpl();
		msg.writeChar('c');
		msg.markAsReadOnly();
		try { msg.readByte(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals('c',msg.readChar());
		
		// Short
		msg = new StreamMessageImpl();
		msg.writeChar('c');
		msg.markAsReadOnly();
		try { msg.readShort(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals('c',msg.readChar());
		
		// Char
		msg = new StreamMessageImpl();
		msg.writeChar('c');
		msg.markAsReadOnly();
		assertEquals('c',msg.readChar());
		
		// Int
		msg = new StreamMessageImpl();
		msg.writeChar('c');
		msg.markAsReadOnly();
		try { msg.readInt(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals('c',msg.readChar());
		
		// Long
		msg = new StreamMessageImpl();
		msg.writeChar('c');
		msg.markAsReadOnly();
		try { msg.readLong(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals('c',msg.readChar());
		
		// Float
		msg = new StreamMessageImpl();
		msg.writeChar('c');
		msg.markAsReadOnly();
		try { msg.readFloat(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals('c',msg.readChar());
		
		// Double
		msg = new StreamMessageImpl();
		msg.writeChar('c');
		msg.markAsReadOnly();
		try { msg.readDouble(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals('c',msg.readChar());
		
		// String
		msg = new StreamMessageImpl();
		msg.writeChar('c');
		msg.markAsReadOnly();
		assertEquals("c",msg.readString());
		
		// Bytes
		msg = new StreamMessageImpl();
		msg.writeChar('c');
		msg.markAsReadOnly();
		try { msg.readBytes(new byte[1]); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals('c',msg.readChar());
	}
	
	public void testIntConversion() throws Exception
	{
		StreamMessageImpl msg;
		
		// Boolean
		msg = new StreamMessageImpl();
		msg.writeInt(123);
		msg.markAsReadOnly();
		try { msg.readBoolean(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readInt());
		
		// Byte
		msg = new StreamMessageImpl();
		msg.writeInt(123);
		msg.markAsReadOnly();
		try { msg.readByte(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readInt());
		
		// Short
		msg = new StreamMessageImpl();
		msg.writeInt(123);
		msg.markAsReadOnly();
		try { msg.readShort(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readInt());
		
		// Char
		msg = new StreamMessageImpl();
		msg.writeInt(123);
		msg.markAsReadOnly();
		try { msg.readChar(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readInt());
		
		// Int
		msg = new StreamMessageImpl();
		msg.writeInt(123);
		msg.markAsReadOnly();
		assertEquals(123,msg.readInt());
		
		// Long
		msg = new StreamMessageImpl();
		msg.writeInt(123);
		msg.markAsReadOnly();
		assertEquals(123,msg.readLong());
		
		// Float
		msg = new StreamMessageImpl();
		msg.writeInt(123);
		msg.markAsReadOnly();
		try { msg.readFloat(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.readInt());
		
		// Double
		msg = new StreamMessageImpl();
		msg.writeInt(123);
		msg.markAsReadOnly();
		try { msg.readDouble(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.readInt());
		
		// String
		msg = new StreamMessageImpl();
		msg.writeInt(123);
		msg.markAsReadOnly();
		assertEquals("123",msg.readString());
		
		// Bytes
		msg = new StreamMessageImpl();
		msg.writeInt(123);
		msg.markAsReadOnly();
		try { msg.readBytes(new byte[1]); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readInt());
	}
	
	public void testLongConversion() throws Exception
	{
		StreamMessageImpl msg;
		
		// Boolean
		msg = new StreamMessageImpl();
		msg.writeLong(123);
		msg.markAsReadOnly();
		try { msg.readBoolean(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readLong());
		
		// Byte
		msg = new StreamMessageImpl();
		msg.writeLong(123);
		msg.markAsReadOnly();
		try { msg.readByte(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readLong());
		
		// Short
		msg = new StreamMessageImpl();
		msg.writeLong(123);
		msg.markAsReadOnly();
		try { msg.readShort(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readLong());
		
		// Char
		msg = new StreamMessageImpl();
		msg.writeLong(123);
		msg.markAsReadOnly();
		try { msg.readChar(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readLong());
		
		// Int
		msg = new StreamMessageImpl();
		msg.writeLong(123);
		msg.markAsReadOnly();
		try { msg.readInt(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.readLong());
		
		// Long
		msg = new StreamMessageImpl();
		msg.writeLong(123);
		msg.markAsReadOnly();
		assertEquals(123,msg.readLong());
		
		// Float
		msg = new StreamMessageImpl();
		msg.writeLong(123);
		msg.markAsReadOnly();
		try { msg.readFloat(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.readLong());
		
		// Double
		msg = new StreamMessageImpl();
		msg.writeLong(123);
		msg.markAsReadOnly();
		try { msg.readDouble(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.readLong());
		
		// String
		msg = new StreamMessageImpl();
		msg.writeLong(123);
		msg.markAsReadOnly();
		assertEquals("123",msg.readString());
		
		// Bytes
		msg = new StreamMessageImpl();
		msg.writeLong(123);
		msg.markAsReadOnly();
		try { msg.readBytes(new byte[1]); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.readLong());
	}
	
	public void testFloatConversion() throws Exception
	{
		StreamMessageImpl msg;
		
		// Boolean
		msg = new StreamMessageImpl();
		msg.writeFloat(1.23f);
		msg.markAsReadOnly();
		try { msg.readBoolean(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23f,msg.readFloat(),0);
		
		// Byte
		msg = new StreamMessageImpl();
		msg.writeFloat(1.23f);
		msg.markAsReadOnly();
		try { msg.readByte(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23f,msg.readFloat(),0);
		
		// Short
		msg = new StreamMessageImpl();
		msg.writeFloat(1.23f);
		msg.markAsReadOnly();
		try { msg.readShort(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23f,msg.readFloat(),0);
		
		// Char
		msg = new StreamMessageImpl();
		msg.writeFloat(1.23f);
		msg.markAsReadOnly();
		try { msg.readChar(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23f,msg.readFloat(),0);
		
		// Int
		msg = new StreamMessageImpl();
		msg.writeFloat(1.23f);
		msg.markAsReadOnly();
		try { msg.readInt(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(1.23f,msg.readFloat(),0);
		
		// Long
		msg = new StreamMessageImpl();
		msg.writeFloat(1.23f);
		msg.markAsReadOnly();
		try { msg.readLong(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(1.23f,msg.readFloat(),0);
		
		// Float
		msg = new StreamMessageImpl();
		msg.writeFloat(1.23f);
		msg.markAsReadOnly();
		assertEquals(1.23f,msg.readFloat(),0);
		
		// Double
		msg = new StreamMessageImpl();
		msg.writeFloat(1.23f);
		msg.markAsReadOnly();
		assertEquals(1.23,msg.readDouble(),0.0000001);
		
		// String
		msg = new StreamMessageImpl();
		msg.writeFloat(1.23f);
		msg.markAsReadOnly();
		assertEquals("1.23",msg.readString());
		
		// Bytes
		msg = new StreamMessageImpl();
		msg.writeFloat(1.23f);
		msg.markAsReadOnly();
		try { msg.readBytes(new byte[1]); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23f,msg.readFloat(),0);
	}
	
	public void testDoubleConversion() throws Exception
	{
		StreamMessageImpl msg;
		
		// Boolean
		msg = new StreamMessageImpl();
		msg.writeDouble(1.23);
		msg.markAsReadOnly();
		try { msg.readBoolean(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23,msg.readDouble(),0);
		
		// Byte
		msg = new StreamMessageImpl();
		msg.writeDouble(1.23);
		msg.markAsReadOnly();
		try { msg.readByte(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23,msg.readDouble(),0);
		
		// Short
		msg = new StreamMessageImpl();
		msg.writeDouble(1.23);
		msg.markAsReadOnly();
		try { msg.readShort(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23,msg.readDouble(),0);
		
		// Char
		msg = new StreamMessageImpl();
		msg.writeDouble(1.23);
		msg.markAsReadOnly();
		try { msg.readChar(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23,msg.readDouble(),0);
		
		// Int
		msg = new StreamMessageImpl();
		msg.writeDouble(1.23);
		msg.markAsReadOnly();
		try { msg.readInt(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(1.23,msg.readDouble(),0);
		
		// Long
		msg = new StreamMessageImpl();
		msg.writeDouble(1.23);
		msg.markAsReadOnly();
		try { msg.readLong(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(1.23,msg.readDouble(),0);
		
		// Float
		msg = new StreamMessageImpl();
		msg.writeDouble(1.23);
		msg.markAsReadOnly();
		try { msg.readFloat(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(1.23,msg.readDouble(),0);
		
		// Double
		msg = new StreamMessageImpl();
		msg.writeDouble(1.23);
		msg.markAsReadOnly();
		assertEquals(1.23,msg.readDouble(),0.0000001);
		
		// String
		msg = new StreamMessageImpl();
		msg.writeDouble(1.23);
		msg.markAsReadOnly();
		assertEquals("1.23",msg.readString());
		
		// Bytes
		msg = new StreamMessageImpl();
		msg.writeDouble(1.23);
		msg.markAsReadOnly();
		try { msg.readBytes(new byte[1]); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23,msg.readDouble(),0);
	}
	
	public void testStringConversion() throws Exception
	{
		StreamMessageImpl msg;
		
		// Boolean
		msg = new StreamMessageImpl();
		msg.writeString("foobar");
		msg.markAsReadOnly();
		assertFalse(msg.readBoolean());
		
		// Byte
		msg = new StreamMessageImpl();
		msg.writeString("foobar");
		msg.markAsReadOnly();
		try { msg.readByte(); fail("Should have failed"); } catch (NumberFormatException e) { /* OK */	}
		assertEquals("foobar",msg.readString());
		
		// Short
		msg = new StreamMessageImpl();
		msg.writeString("foobar");
		msg.markAsReadOnly();
		try { msg.readShort(); fail("Should have failed"); } catch (NumberFormatException e) { /* OK */	}
		assertEquals("foobar",msg.readString());
		
		// Char
		msg = new StreamMessageImpl();
		msg.writeString("foobar");
		msg.markAsReadOnly();
		try { msg.readChar(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals("foobar",msg.readString());
		
		// Int
		msg = new StreamMessageImpl();
		msg.writeString("foobar");
		msg.markAsReadOnly();
		try { msg.readInt(); fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		assertEquals("foobar",msg.readString());
		
		// Long
		msg = new StreamMessageImpl();
		msg.writeString("foobar");
		msg.markAsReadOnly();
		try { msg.readLong(); fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		assertEquals("foobar",msg.readString());
		
		// Float
		msg = new StreamMessageImpl();
		msg.writeString("foobar");
		msg.markAsReadOnly();
		try { msg.readFloat(); fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		assertEquals("foobar",msg.readString());
		
		// Double
		msg = new StreamMessageImpl();
		msg.writeString("foobar");
		msg.markAsReadOnly();
		try { msg.readDouble(); fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		assertEquals("foobar",msg.readString());
		
		// String
		msg = new StreamMessageImpl();
		msg.writeString("foobar");
		msg.markAsReadOnly();
		assertEquals("foobar",msg.readString());
		
		// Bytes
		msg = new StreamMessageImpl();
		msg.writeString("foobar");
		msg.markAsReadOnly();
		try { msg.readBytes(new byte[1]); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals("foobar",msg.readString());
	}
	
	public void testNumericStringConversion() throws Exception
	{
		StreamMessageImpl msg;
		
		// Boolean
		msg = new StreamMessageImpl();
		msg.writeString("123");
		msg.markAsReadOnly();
		assertFalse(msg.readBoolean());
		
		// Byte
		msg = new StreamMessageImpl();
		msg.writeString("123");
		msg.markAsReadOnly();
		assertEquals((byte)123,msg.readByte());
		
		// Short
		msg = new StreamMessageImpl();
		msg.writeString("123");
		msg.markAsReadOnly();
		assertEquals(123,msg.readShort());
		
		// Char
		msg = new StreamMessageImpl();
		msg.writeString("123");
		msg.markAsReadOnly();
		try { msg.readChar(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals("123",msg.readString());
		
		// Int
		msg = new StreamMessageImpl();
		msg.writeString("123");
		msg.markAsReadOnly();
		assertEquals(123,msg.readInt());
		
		// Long
		msg = new StreamMessageImpl();
		msg.writeString("123");
		msg.markAsReadOnly();
		assertEquals(123,msg.readLong());
		
		// Float
		msg = new StreamMessageImpl();
		msg.writeString("123");
		msg.markAsReadOnly();
		assertEquals(123,msg.readFloat(),0);
		
		// Double
		msg = new StreamMessageImpl();
		msg.writeString("123");
		msg.markAsReadOnly();
		assertEquals(123,msg.readDouble(),0);
		
		// String
		msg = new StreamMessageImpl();
		msg.writeString("123");
		msg.markAsReadOnly();
		assertEquals("123",msg.readString());
		
		// Bytes
		msg = new StreamMessageImpl();
		msg.writeString("123");
		msg.markAsReadOnly();
		try { msg.readBytes(new byte[1]); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals("123",msg.readString());
	}
	
	public void testBytesConversion() throws Exception
	{
		StreamMessageImpl msg;
		
		byte[] dummy = { (byte)1 , (byte)2 , (byte)3 };
		
		// Boolean
		msg = new StreamMessageImpl();
		msg.writeBytes(dummy);
		msg.markAsReadOnly();
		try { msg.readBoolean(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// Byte
		msg = new StreamMessageImpl();
		msg.writeBytes(dummy);
		msg.markAsReadOnly();
		try { msg.readByte(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// Short
		msg = new StreamMessageImpl();
		msg.writeBytes(dummy);
		msg.markAsReadOnly();
		try { msg.readShort(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// Char
		msg = new StreamMessageImpl();
		msg.writeBytes(dummy);
		msg.markAsReadOnly();
		try { msg.readChar(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// Int
		msg = new StreamMessageImpl();
		msg.writeBytes(dummy);
		msg.markAsReadOnly();
		try { msg.readInt(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// Long
		msg = new StreamMessageImpl();
		msg.writeBytes(dummy);
		msg.markAsReadOnly();
		try { msg.readLong(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// Float
		msg = new StreamMessageImpl();
		msg.writeBytes(dummy);
		msg.markAsReadOnly();
		try { msg.readFloat(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// Double
		msg = new StreamMessageImpl();
		msg.writeBytes(dummy);
		msg.markAsReadOnly();
		try { msg.readDouble(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// String
		msg = new StreamMessageImpl();
		msg.writeBytes(dummy);
		msg.markAsReadOnly();
		try { msg.readString(); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(3,msg.readBytes(new byte[3]));
		
		// Bytes
		msg = new StreamMessageImpl();
		msg.writeBytes(dummy);
		msg.markAsReadOnly();
		byte[] data = new byte[3];
		assertEquals(3,msg.readBytes(data));
		System.out.println(data[0]);
		assertEquals(1, data[0]);
		assertEquals(2, data[1]);
		assertEquals(3, data[2]);
	}
}
