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
import net.timewalker.ffmq3.common.message.MapMessageImpl;

/**
 * MapMessageImplTest
 */
public class MapMessageImplTest extends TestCase
{
	public void testBooleanConversion() throws Exception
	{
		MapMessageImpl msg;
		
		// Boolean
		msg = new MapMessageImpl();
		msg.setBoolean("prop",true);
		msg.markAsReadOnly();
		assertTrue(msg.getBoolean("prop"));
		
		// Byte
		msg = new MapMessageImpl();
		msg.setBoolean("prop",true);
		msg.markAsReadOnly();
		try { msg.getByte("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertTrue(msg.getBoolean("prop"));
		
		// Short
		msg = new MapMessageImpl();
		msg.setBoolean("prop",true);
		msg.markAsReadOnly();
		try { msg.getShort("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertTrue(msg.getBoolean("prop"));

		// Char
		msg = new MapMessageImpl();
		msg.setBoolean("prop",true);
		msg.markAsReadOnly();
		try { msg.getChar("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertTrue(msg.getBoolean("prop"));
		
		// Int
		msg = new MapMessageImpl();
		msg.setBoolean("prop",true);
		msg.markAsReadOnly();
		try { msg.getInt("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertTrue(msg.getBoolean("prop"));
		
		// Long
		msg = new MapMessageImpl();
		msg.setBoolean("prop",true);
		msg.markAsReadOnly();
		try { msg.getLong("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertTrue(msg.getBoolean("prop"));
		
		// Float
		msg = new MapMessageImpl();
		msg.setBoolean("prop",true);
		msg.markAsReadOnly();
		try { msg.getFloat("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertTrue(msg.getBoolean("prop"));
		
		// Double
		msg = new MapMessageImpl();
		msg.setBoolean("prop",true);
		msg.markAsReadOnly();
		try { msg.getDouble("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertTrue(msg.getBoolean("prop"));
		
		// String
		msg = new MapMessageImpl();
		msg.setBoolean("prop",true);
		msg.markAsReadOnly();
		assertEquals("true",msg.getString("prop"));
		
		// Bytes
		msg = new MapMessageImpl();
		msg.setBoolean("prop",true);
		msg.markAsReadOnly();
		try { msg.getBytes("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertTrue(msg.getBoolean("prop"));
	}

	public void testByteConversion() throws Exception
	{
		MapMessageImpl msg;
		
		// Boolean
		msg = new MapMessageImpl();
		msg.setByte("prop",(byte)123);
		msg.markAsReadOnly();
		try { msg.getBoolean("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getByte("prop"));
		
		// Byte
		msg = new MapMessageImpl();
		msg.setByte("prop",(byte)123);
		msg.markAsReadOnly();
		assertEquals(123,msg.getByte("prop"));
		
		// Short
		msg = new MapMessageImpl();
		msg.setByte("prop",(byte)123);
		msg.markAsReadOnly();
		assertEquals(123,msg.getShort("prop"));
		
		// Char
		msg = new MapMessageImpl();
		msg.setByte("prop",(byte)123);
		msg.markAsReadOnly();
		try { msg.getChar("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getByte("prop"));
		
		// Int
		msg = new MapMessageImpl();
		msg.setByte("prop",(byte)123);
		msg.markAsReadOnly();
		assertEquals(123,msg.getInt("prop"));
		
		// Long
		msg = new MapMessageImpl();
		msg.setByte("prop",(byte)123);
		msg.markAsReadOnly();
		assertEquals(123,msg.getLong("prop"));
		
		// Float
		msg = new MapMessageImpl();
		msg.setByte("prop",(byte)123);
		msg.markAsReadOnly();
		try { msg.getFloat("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.getByte("prop"));
		
		// Double
		msg = new MapMessageImpl();
		msg.setByte("prop",(byte)123);
		msg.markAsReadOnly();
		try { msg.getDouble("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.getByte("prop"));
		
		// String
		msg = new MapMessageImpl();
		msg.setByte("prop",(byte)123);
		msg.markAsReadOnly();
		assertEquals("123",msg.getString("prop"));
		
		// Bytes
		msg = new MapMessageImpl();
		msg.setByte("prop",(byte)123);
		msg.markAsReadOnly();
		try { msg.getBytes("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getByte("prop"));
	}
	
	public void testShortConversion() throws Exception
	{
		MapMessageImpl msg;
		
		// Boolean
		msg = new MapMessageImpl();
		msg.setShort("prop",(short)123);
		msg.markAsReadOnly();
		try { msg.getBoolean("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getShort("prop"));
		
		// Byte
		msg = new MapMessageImpl();
		msg.setShort("prop",(short)123);
		msg.markAsReadOnly();
		try { msg.getByte("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getShort("prop"));
		
		// Short
		msg = new MapMessageImpl();
		msg.setShort("prop",(short)123);
		msg.markAsReadOnly();
		assertEquals(123,msg.getShort("prop"));
		
		// Char
		msg = new MapMessageImpl();
		msg.setShort("prop",(short)123);
		msg.markAsReadOnly();
		try { msg.getChar("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getShort("prop"));
		
		// Int
		msg = new MapMessageImpl();
		msg.setShort("prop",(short)123);
		msg.markAsReadOnly();
		assertEquals(123,msg.getInt("prop"));
		
		// Long
		msg = new MapMessageImpl();
		msg.setShort("prop",(short)123);
		msg.markAsReadOnly();
		assertEquals(123,msg.getLong("prop"));
		
		// Float
		msg = new MapMessageImpl();
		msg.setShort("prop",(short)123);
		msg.markAsReadOnly();
		try { msg.getFloat("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.getShort("prop"));
		
		// Double
		msg = new MapMessageImpl();
		msg.setShort("prop",(short)123);
		msg.markAsReadOnly();
		try { msg.getDouble("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.getShort("prop"));
		
		// String
		msg = new MapMessageImpl();
		msg.setShort("prop",(short)123);
		msg.markAsReadOnly();
		assertEquals("123",msg.getString("prop"));
		
		// Bytes
		msg = new MapMessageImpl();
		msg.setShort("prop",(short)123);
		msg.markAsReadOnly();
		try { msg.getBytes("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getShort("prop"));
	}
	
	public void testCharConversion() throws Exception
	{
		MapMessageImpl msg;
		
		// Boolean
		msg = new MapMessageImpl();
		msg.setChar("prop",'c');
		msg.markAsReadOnly();
		try { msg.getBoolean("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals('c',msg.getChar("prop"));
		
		// Byte
		msg = new MapMessageImpl();
		msg.setChar("prop",'c');
		msg.markAsReadOnly();
		try { msg.getByte("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals('c',msg.getChar("prop"));
		
		// Short
		msg = new MapMessageImpl();
		msg.setChar("prop",'c');
		msg.markAsReadOnly();
		try { msg.getShort("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals('c',msg.getChar("prop"));
		
		// Char
		msg = new MapMessageImpl();
		msg.setChar("prop",'c');
		msg.markAsReadOnly();
		assertEquals('c',msg.getChar("prop"));
		
		// Int
		msg = new MapMessageImpl();
		msg.setChar("prop",'c');
		msg.markAsReadOnly();
		try { msg.getInt("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals('c',msg.getChar("prop"));
		
		// Long
		msg = new MapMessageImpl();
		msg.setChar("prop",'c');
		msg.markAsReadOnly();
		try { msg.getLong("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals('c',msg.getChar("prop"));
		
		// Float
		msg = new MapMessageImpl();
		msg.setChar("prop",'c');
		msg.markAsReadOnly();
		try { msg.getFloat("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals('c',msg.getChar("prop"));
		
		// Double
		msg = new MapMessageImpl();
		msg.setChar("prop",'c');
		msg.markAsReadOnly();
		try { msg.getDouble("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals('c',msg.getChar("prop"));
		
		// String
		msg = new MapMessageImpl();
		msg.setChar("prop",'c');
		msg.markAsReadOnly();
		assertEquals("c",msg.getString("prop"));
		
		// Bytes
		msg = new MapMessageImpl();
		msg.setChar("prop",'c');
		msg.markAsReadOnly();
		try { msg.getBytes("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals('c',msg.getChar("prop"));
	}
	
	public void testIntConversion() throws Exception
	{
		MapMessageImpl msg;
		
		// Boolean
		msg = new MapMessageImpl();
		msg.setInt("prop",123);
		msg.markAsReadOnly();
		try { msg.getBoolean("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getInt("prop"));
		
		// Byte
		msg = new MapMessageImpl();
		msg.setInt("prop",123);
		msg.markAsReadOnly();
		try { msg.getByte("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getInt("prop"));
		
		// Short
		msg = new MapMessageImpl();
		msg.setInt("prop",123);
		msg.markAsReadOnly();
		try { msg.getShort("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getInt("prop"));
		
		// Char
		msg = new MapMessageImpl();
		msg.setInt("prop",123);
		msg.markAsReadOnly();
		try { msg.getChar("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getInt("prop"));
		
		// Int
		msg = new MapMessageImpl();
		msg.setInt("prop",123);
		msg.markAsReadOnly();
		assertEquals(123,msg.getInt("prop"));
		
		// Long
		msg = new MapMessageImpl();
		msg.setInt("prop",123);
		msg.markAsReadOnly();
		assertEquals(123,msg.getLong("prop"));
		
		// Float
		msg = new MapMessageImpl();
		msg.setInt("prop",123);
		msg.markAsReadOnly();
		try { msg.getFloat("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.getInt("prop"));
		
		// Double
		msg = new MapMessageImpl();
		msg.setInt("prop",123);
		msg.markAsReadOnly();
		try { msg.getDouble("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.getInt("prop"));
		
		// String
		msg = new MapMessageImpl();
		msg.setInt("prop",123);
		msg.markAsReadOnly();
		assertEquals("123",msg.getString("prop"));
		
		// Bytes
		msg = new MapMessageImpl();
		msg.setInt("prop",123);
		msg.markAsReadOnly();
		try { msg.getBytes("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getInt("prop"));
	}
	
	public void testLongConversion() throws Exception
	{
		MapMessageImpl msg;
		
		// Boolean
		msg = new MapMessageImpl();
		msg.setLong("prop",123);
		msg.markAsReadOnly();
		try { msg.getBoolean("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getLong("prop"));
		
		// Byte
		msg = new MapMessageImpl();
		msg.setLong("prop",123);
		msg.markAsReadOnly();
		try { msg.getByte("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getLong("prop"));
		
		// Short
		msg = new MapMessageImpl();
		msg.setLong("prop",123);
		msg.markAsReadOnly();
		try { msg.getShort("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getLong("prop"));
		
		// Char
		msg = new MapMessageImpl();
		msg.setLong("prop",123);
		msg.markAsReadOnly();
		try { msg.getChar("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getLong("prop"));
		
		// Int
		msg = new MapMessageImpl();
		msg.setLong("prop",123);
		msg.markAsReadOnly();
		try { msg.getInt("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.getLong("prop"));
		
		// Long
		msg = new MapMessageImpl();
		msg.setLong("prop",123);
		msg.markAsReadOnly();
		assertEquals(123,msg.getLong("prop"));
		
		// Float
		msg = new MapMessageImpl();
		msg.setLong("prop",123);
		msg.markAsReadOnly();
		try { msg.getFloat("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.getLong("prop"));
		
		// Double
		msg = new MapMessageImpl();
		msg.setLong("prop",123);
		msg.markAsReadOnly();
		try { msg.getDouble("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(123,msg.getLong("prop"));
		
		// String
		msg = new MapMessageImpl();
		msg.setLong("prop",123);
		msg.markAsReadOnly();
		assertEquals("123",msg.getString("prop"));
		
		// Bytes
		msg = new MapMessageImpl();
		msg.setLong("prop",123);
		msg.markAsReadOnly();
		try { msg.getBytes("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(123,msg.getLong("prop"));
	}
	
	public void testFloatConversion() throws Exception
	{
		MapMessageImpl msg;
		
		// Boolean
		msg = new MapMessageImpl();
		msg.setFloat("prop",1.23f);
		msg.markAsReadOnly();
		try { msg.getBoolean("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23f,msg.getFloat("prop"),0);
		
		// Byte
		msg = new MapMessageImpl();
		msg.setFloat("prop",1.23f);
		msg.markAsReadOnly();
		try { msg.getByte("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23f,msg.getFloat("prop"),0);
		
		// Short
		msg = new MapMessageImpl();
		msg.setFloat("prop",1.23f);
		msg.markAsReadOnly();
		try { msg.getShort("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23f,msg.getFloat("prop"),0);
		
		// Char
		msg = new MapMessageImpl();
		msg.setFloat("prop",1.23f);
		msg.markAsReadOnly();
		try { msg.getChar("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23f,msg.getFloat("prop"),0);
		
		// Int
		msg = new MapMessageImpl();
		msg.setFloat("prop",1.23f);
		msg.markAsReadOnly();
		try { msg.getInt("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(1.23f,msg.getFloat("prop"),0);
		
		// Long
		msg = new MapMessageImpl();
		msg.setFloat("prop",1.23f);
		msg.markAsReadOnly();
		try { msg.getLong("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(1.23f,msg.getFloat("prop"),0);
		
		// Float
		msg = new MapMessageImpl();
		msg.setFloat("prop",1.23f);
		msg.markAsReadOnly();
		assertEquals(1.23f,msg.getFloat("prop"),0);
		
		// Double
		msg = new MapMessageImpl();
		msg.setFloat("prop",1.23f);
		msg.markAsReadOnly();
		assertEquals(1.23,msg.getDouble("prop"),0.0000001);
		
		// String
		msg = new MapMessageImpl();
		msg.setFloat("prop",1.23f);
		msg.markAsReadOnly();
		assertEquals("1.23",msg.getString("prop"));
		
		// Bytes
		msg = new MapMessageImpl();
		msg.setFloat("prop",1.23f);
		msg.markAsReadOnly();
		try { msg.getBytes("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23f,msg.getFloat("prop"),0);
	}
	
	public void testDoubleConversion() throws Exception
	{
		MapMessageImpl msg;
		
		// Boolean
		msg = new MapMessageImpl();
		msg.setDouble("prop",1.23);
		msg.markAsReadOnly();
		try { msg.getBoolean("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23,msg.getDouble("prop"),0);
		
		// Byte
		msg = new MapMessageImpl();
		msg.setDouble("prop",1.23);
		msg.markAsReadOnly();
		try { msg.getByte("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23,msg.getDouble("prop"),0);
		
		// Short
		msg = new MapMessageImpl();
		msg.setDouble("prop",1.23);
		msg.markAsReadOnly();
		try { msg.getShort("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23,msg.getDouble("prop"),0);
		
		// Char
		msg = new MapMessageImpl();
		msg.setDouble("prop",1.23);
		msg.markAsReadOnly();
		try { msg.getChar("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23,msg.getDouble("prop"),0);
		
		// Int
		msg = new MapMessageImpl();
		msg.setDouble("prop",1.23);
		msg.markAsReadOnly();
		try { msg.getInt("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(1.23,msg.getDouble("prop"),0);
		
		// Long
		msg = new MapMessageImpl();
		msg.setDouble("prop",1.23);
		msg.markAsReadOnly();
		try { msg.getLong("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(1.23,msg.getDouble("prop"),0);
		
		// Float
		msg = new MapMessageImpl();
		msg.setDouble("prop",1.23);
		msg.markAsReadOnly();
		try { msg.getFloat("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(1.23,msg.getDouble("prop"),0);
		
		// Double
		msg = new MapMessageImpl();
		msg.setDouble("prop",1.23);
		msg.markAsReadOnly();
		assertEquals(1.23,msg.getDouble("prop"),0.0000001);
		
		// String
		msg = new MapMessageImpl();
		msg.setDouble("prop",1.23);
		msg.markAsReadOnly();
		assertEquals("1.23",msg.getString("prop"));
		
		// Bytes
		msg = new MapMessageImpl();
		msg.setDouble("prop",1.23);
		msg.markAsReadOnly();
		try { msg.getBytes("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(1.23,msg.getDouble("prop"),0);
	}
	
	public void testStringConversion() throws Exception
	{
		MapMessageImpl msg;
		
		// Boolean
		msg = new MapMessageImpl();
		msg.setString("prop","foobar");
		msg.markAsReadOnly();
		assertFalse(msg.getBoolean("prop"));
		
		// Byte
		msg = new MapMessageImpl();
		msg.setString("prop","foobar");
		msg.markAsReadOnly();
		try { msg.getByte("prop"); fail("Should have failed"); } catch (NumberFormatException e) { /* OK */	}
		assertEquals("foobar",msg.getString("prop"));
		
		// Short
		msg = new MapMessageImpl();
		msg.setString("prop","foobar");
		msg.markAsReadOnly();
		try { msg.getShort("prop"); fail("Should have failed"); } catch (NumberFormatException e) { /* OK */	}
		assertEquals("foobar",msg.getString("prop"));
		
		// Char
		msg = new MapMessageImpl();
		msg.setString("prop","foobar");
		msg.markAsReadOnly();
		try { msg.getChar("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals("foobar",msg.getString("prop"));
		
		// Int
		msg = new MapMessageImpl();
		msg.setString("prop","foobar");
		msg.markAsReadOnly();
		try { msg.getInt("prop"); fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		assertEquals("foobar",msg.getString("prop"));
		
		// Long
		msg = new MapMessageImpl();
		msg.setString("prop","foobar");
		msg.markAsReadOnly();
		try { msg.getLong("prop"); fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		assertEquals("foobar",msg.getString("prop"));
		
		// Float
		msg = new MapMessageImpl();
		msg.setString("prop","foobar");
		msg.markAsReadOnly();
		try { msg.getFloat("prop"); fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		assertEquals("foobar",msg.getString("prop"));
		
		// Double
		msg = new MapMessageImpl();
		msg.setString("prop","foobar");
		msg.markAsReadOnly();
		try { msg.getDouble("prop"); fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		assertEquals("foobar",msg.getString("prop"));
		
		// String
		msg = new MapMessageImpl();
		msg.setString("prop","foobar");
		msg.markAsReadOnly();
		assertEquals("foobar",msg.getString("prop"));
		
		// Bytes
		msg = new MapMessageImpl();
		msg.setString("prop","foobar");
		msg.markAsReadOnly();
		try { msg.getBytes("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals("foobar",msg.getString("prop"));
	}
	
	public void testNumericStringConversion() throws Exception
	{
		MapMessageImpl msg;
		
		// Boolean
		msg = new MapMessageImpl();
		msg.setString("prop","123");
		msg.markAsReadOnly();
		assertFalse(msg.getBoolean("prop"));
		
		// Byte
		msg = new MapMessageImpl();
		msg.setString("prop","123");
		msg.markAsReadOnly();
		assertEquals((byte)123,msg.getByte("prop"));
		
		// Short
		msg = new MapMessageImpl();
		msg.setString("prop","123");
		msg.markAsReadOnly();
		assertEquals(123,msg.getShort("prop"));
		
		// Char
		msg = new MapMessageImpl();
		msg.setString("prop","123");
		msg.markAsReadOnly();
		try { msg.getChar("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals("123",msg.getString("prop"));
		
		// Int
		msg = new MapMessageImpl();
		msg.setString("prop","123");
		msg.markAsReadOnly();
		assertEquals(123,msg.getInt("prop"));
		
		// Long
		msg = new MapMessageImpl();
		msg.setString("prop","123");
		msg.markAsReadOnly();
		assertEquals(123,msg.getLong("prop"));
		
		// Float
		msg = new MapMessageImpl();
		msg.setString("prop","123");
		msg.markAsReadOnly();
		assertEquals(123,msg.getFloat("prop"),0);
		
		// Double
		msg = new MapMessageImpl();
		msg.setString("prop","123");
		msg.markAsReadOnly();
		assertEquals(123,msg.getDouble("prop"),0);
		
		// String
		msg = new MapMessageImpl();
		msg.setString("prop","123");
		msg.markAsReadOnly();
		assertEquals("123",msg.getString("prop"));
		
		// Bytes
		msg = new MapMessageImpl();
		msg.setString("prop","123");
		msg.markAsReadOnly();
		try { msg.getBytes("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals("123",msg.getString("prop"));
	}
	
	public void testBytesConversion() throws Exception
	{
		MapMessageImpl msg;
		
		byte[] dummy = { (byte)1 , (byte)2 , (byte)3 };
		
		// Boolean
		msg = new MapMessageImpl();
		msg.setBytes("prop",dummy);
		msg.markAsReadOnly();
		try { msg.getBoolean("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(3,msg.getBytes("prop").length);
		
		// Byte
		msg = new MapMessageImpl();
		msg.setBytes("prop",dummy);
		msg.markAsReadOnly();
		try { msg.getByte("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(3,msg.getBytes("prop").length);
		
		// Short
		msg = new MapMessageImpl();
		msg.setBytes("prop",dummy);
		msg.markAsReadOnly();
		try { msg.getShort("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(3,msg.getBytes("prop").length);
		
		// Char
		msg = new MapMessageImpl();
		msg.setBytes("prop",dummy);
		msg.markAsReadOnly();
		try { msg.getChar("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */	}
		assertEquals(3,msg.getBytes("prop").length);
		
		// Int
		msg = new MapMessageImpl();
		msg.setBytes("prop",dummy);
		msg.markAsReadOnly();
		try { msg.getInt("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(3,msg.getBytes("prop").length);
		
		// Long
		msg = new MapMessageImpl();
		msg.setBytes("prop",dummy);
		msg.markAsReadOnly();
		try { msg.getLong("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(3,msg.getBytes("prop").length);
		
		// Float
		msg = new MapMessageImpl();
		msg.setBytes("prop",dummy);
		msg.markAsReadOnly();
		try { msg.getFloat("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(3,msg.getBytes("prop").length);
		
		// Double
		msg = new MapMessageImpl();
		msg.setBytes("prop",dummy);
		msg.markAsReadOnly();
		try { msg.getDouble("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(3,msg.getBytes("prop").length);
		
		// String
		msg = new MapMessageImpl();
		msg.setBytes("prop",dummy);
		msg.markAsReadOnly();
		try { msg.getString("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		assertEquals(3,msg.getBytes("prop").length);
		
		// Bytes
		msg = new MapMessageImpl();
		msg.setBytes("prop",dummy);
		msg.markAsReadOnly();
		byte[] data = msg.getBytes("prop");
		assertEquals(3,data.length);
		assertEquals(1, data[0]);
		assertEquals(2, data[1]);
		assertEquals(3, data[2]);
	}
}
