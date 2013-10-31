package net.timewalker.ffmq3.test.common.message.selector.expression.utils;

import junit.framework.TestCase;
import net.timewalker.ffmq3.common.message.selector.expression.utils.StringUtils;

public class StringUtilsTest
    extends TestCase
{
	public void testReplaceDoubleSingleQuotes()
	{
		assertEquals("",StringUtils.replaceDoubleSingleQuotes(""));
		assertEquals("'",StringUtils.replaceDoubleSingleQuotes("''"));
		assertEquals("''",StringUtils.replaceDoubleSingleQuotes("''''"));
		assertEquals("foo",StringUtils.replaceDoubleSingleQuotes("foo"));
		assertEquals("foo'bar",StringUtils.replaceDoubleSingleQuotes("foo''bar"));
		assertEquals("'foobar",StringUtils.replaceDoubleSingleQuotes("''foobar"));
		assertEquals("foobar'",StringUtils.replaceDoubleSingleQuotes("foobar''"));
		assertEquals("'foobar'",StringUtils.replaceDoubleSingleQuotes("''foobar''"));
		assertEquals("'fo'obar'",StringUtils.replaceDoubleSingleQuotes("''fo''obar''"));
		
		assertEquals("'",StringUtils.replaceDoubleSingleQuotes("'"));
		assertEquals("foo'bar",StringUtils.replaceDoubleSingleQuotes("foo'bar"));
		assertEquals("'foobar",StringUtils.replaceDoubleSingleQuotes("'foobar"));
		assertEquals("foobar'",StringUtils.replaceDoubleSingleQuotes("foobar'"));
		assertEquals("'foobar'",StringUtils.replaceDoubleSingleQuotes("'foobar'"));
		assertEquals("'fo'obar'",StringUtils.replaceDoubleSingleQuotes("'fo'obar'"));
		
		assertEquals("''",StringUtils.replaceDoubleSingleQuotes("'''"));
		assertEquals("'foo'bar",StringUtils.replaceDoubleSingleQuotes("'foo''bar"));
		assertEquals("''foobar",StringUtils.replaceDoubleSingleQuotes("''''foobar"));
		assertEquals("foobar''",StringUtils.replaceDoubleSingleQuotes("foobar'''"));
		assertEquals("'foo'bar'",StringUtils.replaceDoubleSingleQuotes("'foo''bar'"));
		assertEquals("'fo'obar'",StringUtils.replaceDoubleSingleQuotes("''fo'obar''"));
	}
	
    public void testParseNumber() throws Exception
    {
        assertEquals(Long.class,StringUtils.parseNumber("1").getClass());
        assertEquals(1,StringUtils.parseNumber("1").longValue());
        
        assertEquals(Long.class,StringUtils.parseNumber("1234567890123456").getClass());
        assertEquals(1234567890123456l,StringUtils.parseNumber("1234567890123456").longValue());
     
        assertEquals(Double.class,StringUtils.parseNumber("1.23").getClass());
        assertTrue(1.23 == StringUtils.parseNumber("1.23").doubleValue());
        
        assertEquals(Double.class,StringUtils.parseNumber("1.23e4").getClass());
        assertTrue(1.23e4 == StringUtils.parseNumber("1.23e4").doubleValue());
        
        assertEquals(Double.class,StringUtils.parseNumber("1E2").getClass());
        assertTrue(1E2 == StringUtils.parseNumber("1E2").doubleValue());
    }

    public void testMatches()
    {
        assertTrue(StringUtils.matches("", "", null).booleanValue());
        assertFalse(StringUtils.matches("goo", "", null).booleanValue());
        assertTrue(StringUtils.matches("", "%", null).booleanValue());
        assertTrue(StringUtils.matches("", "%%", null).booleanValue());
        assertTrue(StringUtils.matches("foo", "foo", null).booleanValue());
        assertTrue(StringUtils.matches("foo", "foo%", null).booleanValue());
        assertTrue(StringUtils.matches("foo", "%foo", null).booleanValue());
        assertTrue(StringUtils.matches("foo", "%foo%", null).booleanValue());
        assertTrue(StringUtils.matches("foo", "%f%", null).booleanValue());
        assertTrue(StringUtils.matches("foo", "%fo%", null).booleanValue());
        assertTrue(StringUtils.matches("foo", "%o", null).booleanValue());
        assertTrue(StringUtils.matches("foo", "%oo", null).booleanValue());
        assertTrue(StringUtils.matches("foo", "f%o", null).booleanValue());
        assertTrue(StringUtils.matches("foo", "%f%o%", null).booleanValue());
        
        assertFalse(StringUtils.matches("x", "", null).booleanValue());
        assertFalse(StringUtils.matches("foo", "xfoo", null).booleanValue());
        assertFalse(StringUtils.matches("foo", "foox", null).booleanValue());
        assertFalse(StringUtils.matches("foo", "xfoo%", null).booleanValue());
        assertFalse(StringUtils.matches("foo", "%foox", null).booleanValue());
        assertFalse(StringUtils.matches("foo", "%foox%", null).booleanValue());
        assertFalse(StringUtils.matches("foo", "%x%", null).booleanValue());
        assertFalse(StringUtils.matches("foo", "%o%f%", null).booleanValue());
        assertFalse(StringUtils.matches("foo", "foo%x", null).booleanValue());
        assertFalse(StringUtils.matches("foo", "foooo", null).booleanValue());
        
        assertTrue(StringUtils.matches("foo", "___", null).booleanValue());
        assertTrue(StringUtils.matches("foo", "fo_", null).booleanValue());
        assertTrue(StringUtils.matches("foo", "_oo", null).booleanValue());
        assertTrue(StringUtils.matches("foo", "f_o%", null).booleanValue());
        assertTrue(StringUtils.matches("foo", "__o", null).booleanValue());
        assertFalse(StringUtils.matches("foo", "_ooo", null).booleanValue());
        
        assertFalse(StringUtils.matches("", "_", null).booleanValue());
        
        assertTrue(StringUtils.matches("_", "\\_", "\\").booleanValue());
        assertFalse(StringUtils.matches("x", "\\_", "\\").booleanValue());
        assertTrue(StringUtils.matches("%", "\\%", "\\").booleanValue());
        assertFalse(StringUtils.matches("\\foo", "\\%", "\\").booleanValue());
    }

}
