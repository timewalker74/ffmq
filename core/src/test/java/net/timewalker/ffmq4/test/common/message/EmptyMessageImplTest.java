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

import java.math.BigInteger;

import javax.jms.Message;
import javax.jms.MessageFormatException;

import junit.framework.TestCase;
import net.timewalker.ffmq4.common.message.EmptyMessageImpl;

/**
 * EmptyMessageTest
 */
public class EmptyMessageImplTest extends TestCase
{
	public void testBooleanConversion() throws Exception
	{
		Message msg = new EmptyMessageImpl();
		
		msg.setBooleanProperty("prop", true);
		
		assertTrue(msg.getBooleanProperty("prop"));
		assertEquals("true",msg.getStringProperty("prop"));
		
		try { msg.getByteProperty("prop");   fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getShortProperty("prop");  fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getIntProperty("prop");    fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getLongProperty("prop");   fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getFloatProperty("prop");  fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getDoubleProperty("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
	}
	
	public void testByteConversion() throws Exception
	{
		Message msg = new EmptyMessageImpl();
		
		msg.setByteProperty("prop", (byte)123);
		
		assertEquals(123,msg.getByteProperty("prop"));
		assertEquals(123,msg.getShortProperty("prop"));
		assertEquals(123,msg.getIntProperty("prop"));
		assertEquals(123,msg.getLongProperty("prop"));
		assertEquals("123",msg.getStringProperty("prop"));
		
		try { msg.getBooleanProperty("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getFloatProperty("prop");   fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getDoubleProperty("prop");  fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
	}
	
	public void testShortConversion() throws Exception
	{
		Message msg = new EmptyMessageImpl();
		
		msg.setShortProperty("prop", (short)1234);
		
		assertEquals(1234,msg.getShortProperty("prop"));
		assertEquals(1234,msg.getIntProperty("prop"));
		assertEquals(1234,msg.getLongProperty("prop"));
		assertEquals("1234",msg.getStringProperty("prop"));
		
		try { msg.getBooleanProperty("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getByteProperty("prop");    fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getFloatProperty("prop");   fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getDoubleProperty("prop");  fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
	}
	
	public void testIntConversion() throws Exception
	{
		Message msg = new EmptyMessageImpl();
		
		msg.setIntProperty("prop", 165535);
		
		assertEquals(165535,msg.getIntProperty("prop"));
		assertEquals(165535,msg.getLongProperty("prop"));
		assertEquals("165535",msg.getStringProperty("prop"));
		
		try { msg.getBooleanProperty("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getByteProperty("prop");    fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getShortProperty("prop");   fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getFloatProperty("prop");   fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getDoubleProperty("prop");  fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
	}
	
	public void testLongConversion() throws Exception
	{
		Message msg = new EmptyMessageImpl();
		
		msg.setLongProperty("prop", 92111222333l);
		
		assertEquals(92111222333l,msg.getLongProperty("prop"));
		assertEquals("92111222333",msg.getStringProperty("prop"));
		
		try { msg.getBooleanProperty("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getByteProperty("prop");    fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getShortProperty("prop");   fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getIntProperty("prop");     fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getFloatProperty("prop");   fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getDoubleProperty("prop");  fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
	}
	
	public void testFloatConversion() throws Exception
	{
		Message msg = new EmptyMessageImpl();
		
		msg.setFloatProperty("prop", 1.23f);
		
		assertEquals(1.23f,msg.getFloatProperty("prop"),0);
		assertEquals(1.23,msg.getDoubleProperty("prop"),0.0000001);
		assertEquals("1.23",msg.getStringProperty("prop"));
		
		try { msg.getBooleanProperty("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getByteProperty("prop");    fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getShortProperty("prop");   fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getIntProperty("prop");     fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getLongProperty("prop");    fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
	}
	
	public void testDoubleConversion() throws Exception
	{
		Message msg = new EmptyMessageImpl();
		
		msg.setDoubleProperty("prop", 1.23);

		assertEquals(1.23,msg.getDoubleProperty("prop"),0);
		assertEquals("1.23",msg.getStringProperty("prop"));
		
		try { msg.getBooleanProperty("prop"); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getByteProperty("prop");    fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getShortProperty("prop");   fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getIntProperty("prop");     fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getLongProperty("prop");    fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.getFloatProperty("prop");   fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
	}
	
	public void testStringConversion() throws Exception
	{
		Message msg = new EmptyMessageImpl();
		
		msg.setStringProperty("prop", "foobar");
		assertEquals("foobar", msg.getStringProperty("prop"));
		assertFalse(msg.getBooleanProperty("prop"));
		try { msg.getByteProperty("prop");   fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		try { msg.getShortProperty("prop");  fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		try { msg.getIntProperty("prop");    fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		try { msg.getLongProperty("prop");   fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		try { msg.getFloatProperty("prop");  fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		try { msg.getDoubleProperty("prop"); fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		
		msg.setStringProperty("prop", "123");
		assertEquals("123", msg.getStringProperty("prop"));
		assertFalse(msg.getBooleanProperty("prop"));
		assertEquals(123,msg.getByteProperty("prop"));
		assertEquals(123,msg.getShortProperty("prop"));
		assertEquals(123,msg.getIntProperty("prop"));
		assertEquals(123,msg.getLongProperty("prop"));
		assertEquals(123,msg.getFloatProperty("prop"),0);
		assertEquals(123,msg.getDoubleProperty("prop"),0);
	}
	
	public void testNullConversion() throws Exception
	{
		Message msg = new EmptyMessageImpl();
		
		assertNull(msg.getStringProperty("prop"));
		assertFalse(msg.getBooleanProperty("prop"));
		try { msg.getByteProperty("prop");   fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		try { msg.getShortProperty("prop");  fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		try { msg.getIntProperty("prop");    fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		try { msg.getLongProperty("prop");   fail("Should have failed"); } catch (NumberFormatException e) { /* OK */ }
		try { msg.getFloatProperty("prop");  fail("Should have failed"); } catch (NullPointerException e) { /* OK */ }
		try { msg.getDoubleProperty("prop"); fail("Should have failed"); } catch (NullPointerException e) { /* OK */ }
	}
	
	public void testObjectProperty() throws Exception
	{
		Message msg = new EmptyMessageImpl();
		
		msg.setObjectProperty("prop", new Boolean(true));
		assertEquals(Boolean.TRUE,msg.getObjectProperty("prop"));
		
		msg.setObjectProperty("prop", Byte.valueOf((byte)123));
		assertEquals(Byte.valueOf((byte)123),msg.getObjectProperty("prop"));
		
		msg.setObjectProperty("prop", Short.valueOf((short)123));
		assertEquals(Short.valueOf((short)123),msg.getObjectProperty("prop"));
		
		msg.setObjectProperty("prop", Integer.valueOf(123));
		assertEquals(Integer.valueOf(123),msg.getObjectProperty("prop"));
		
		msg.setObjectProperty("prop", Long.valueOf(123));
		assertEquals(Long.valueOf(123),msg.getObjectProperty("prop"));
		
		msg.setObjectProperty("prop", new Float(123));
		assertEquals(new Float(123),msg.getObjectProperty("prop"));
		
		msg.setObjectProperty("prop", new Double(123));
		assertEquals(new Double(123),msg.getObjectProperty("prop"));
		
		msg.setObjectProperty("prop", "foobar");
		assertEquals("foobar",msg.getObjectProperty("prop"));
		
		try { msg.setObjectProperty("prop", new Object()); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		try { msg.setObjectProperty("prop", new BigInteger("1000")); fail("Should have failed"); } catch (MessageFormatException e) { /* OK */ }
		
		
		assertNull(msg.getObjectProperty("invalid"));
	}
	
	public void testJMSProperties() throws Exception
	{
		Message msg = new EmptyMessageImpl();
		
		msg.setStringProperty("JMSMessageID", "foo");
	}
	
	public void testClearBody() throws Exception
	{
		Message msg = new EmptyMessageImpl();
		msg.clearBody();
	}
}
