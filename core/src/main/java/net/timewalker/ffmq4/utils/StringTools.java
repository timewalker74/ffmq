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
package net.timewalker.ffmq4.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * StringTools
 */
public class StringTools
{
    /**
     * Test if the given text is null or an empty string
     */
    public static boolean isEmpty( String text )
    {
        return text == null || text.length() == 0;
    }
    
    /**
     * Test if the given text is not null and not an empty string
     */
    public static boolean isNotEmpty( String text )
    {
        return text != null && text.length() > 0;
    }
    
    /**
     * Right pad the given string
     */
    public static String rightPad( String value , int size , char padChar )
    {
        if (value.length() >= size)
            return value;
        
        StringBuilder result = new StringBuilder(size);
        result.append(value);
        for (int n = 0 ; n < size-value.length() ; n++)
            result.append(padChar);        
        return result.toString();
    }
    
    /**
     * Case-aware version of startsWith() 
     */
    public static boolean startsWith( String text , String prefix , int offset , boolean ignoreCase )
    {
        if (ignoreCase)
            return startsWithIgnoreCase(text,prefix,offset);
        else
            return text.startsWith(prefix,offset);
    }
    
    /**
     * Case-insensitive version of startsWith() 
     */
    public static boolean startsWithIgnoreCase( String text , String prefix , int offset )
    {
        int textLen = text.length();
        int prefixLen = prefix.length();
        
        if (offset < 0 || (offset > textLen - prefixLen))
            return false;
        
        for(int n=0;n<prefixLen;n++)
        {
            char u1 = Character.toUpperCase(text.charAt(n+offset));
            char u2 = Character.toUpperCase(prefix.charAt(n));
            if (u1 != u2)
                return false;
        }
        
        return true;
    }
    
    /**
     * Case-aware version of startsWith() 
     */
    public static int indexOf( String text , String needle , int fromIndex , boolean ignoreCase )
    {
        if (ignoreCase)
            return indexOfIgnoreCase(text,needle,fromIndex);
        else
            return text.indexOf(needle,fromIndex);
    }
    
    /**
     * Case-insensitive version of indexOf() 
     */
    public static int indexOfIgnoreCase( String text , String needle , int fromIndex )
    {
        int textLen = text.length();
        int needleLen = needle.length();
        
        if (fromIndex >= textLen) return needleLen == 0 ? textLen : -1;
        int i = fromIndex;
        if (i < 0)
        	i = 0;
        if (needleLen == 0) return i;
        
        char first = Character.toUpperCase(needle.charAt(0));
        int max = textLen - needleLen;
        
        startSearchForFirstChar:
        while (true)
        {
            // Look for first character.
            while (i <= max)
            {
                char c = Character.toUpperCase(text.charAt(i));
                if (c != first)
                    i++;
                else
                    break;
            }
            if (i > max) return -1;
            
            // Found first character, now look at the rest of the string
            int j = i + 1;
            int end = j + needleLen - 1;
            int k = 1;
            while (j < end) 
            {
                char c1 = Character.toUpperCase(text.charAt(j++));
                char c2 = Character.toUpperCase(needle.charAt(k++));
                if (c1 != c2) 
                {
                    i++;
                    // Look for str's first char again.
                    continue startSearchForFirstChar;
                }
             }
            
            return i;
        }
    }
    
    /**
     * Check if the given text matches a pattern
     */
    public static boolean matches( String text , String pattern )
    {
        return matches(text,pattern,false);
    }
    
    /**
     * Check if the given text matches a pattern
     */
    public static boolean matchesIgnoreCase( String text , String pattern )
    {
        return matches(text,pattern,true);
    }
        
    /**
     * Check if the given text matches a pattern
     */
    protected static boolean matches( String text , String pattern , boolean ignoreCase )
    {
        if (text == null)
            return false;
        if (pattern.length() == 0)
            return text.length() == 0;
        
        int textPos = 0;
        boolean startWithAny = false;
        StringTokenizer st = new StringTokenizer(pattern,"*?",true);
        while (st.hasMoreTokens())
        {
            String subPattern = st.nextToken();
            
            if (subPattern.equals("*"))
            {
                startWithAny = true;
            }
            else
            if (subPattern.equals("?"))
            {
                if (textPos < text.length())
                    textPos++;
                else
                    return false;
            }
            else
            {
                if (startWithAny)
                {
                    textPos = indexOf(text,subPattern,textPos,ignoreCase);
                    if (textPos == -1) return false;
                    startWithAny = false;
                }
                else
                {
                    if (!startsWith(text,subPattern,textPos,ignoreCase))
                        return false;
                }
                textPos += subPattern.length();
            }
        }
        
        // Remaining text
        if (textPos < text.length())
            if (!startWithAny)
                return false;
        
        return true;
    }
    
    /**
     * Capitalize the given string (null safe)
     * @param text String to capitalize
     * @return the capitalized string
     */
    public static String capitalize( String text )
    {
        if (text == null || text.length() == 0)
            return text;
        
        if (Character.isLowerCase(text.charAt(0)))
            return Character.toUpperCase(text.charAt(0)) + text.substring(1);
        else
            return text;
    }
    
    public static String join( Collection<?> objects , String separator )
    {
    	StringBuilder sb = new StringBuilder();
        
        int count = 0;
        Iterator<?> allObjects = objects.iterator();
        while (allObjects.hasNext())
        {
            Object object = allObjects.next();
            if (count++ > 0)
                sb.append(separator);
            sb.append(String.valueOf(object));
        }
        
        return sb.toString();
    }
    
    public static String join( Object[] objects , String separator )
    {
    	StringBuilder sb = new StringBuilder();
        for (int n = 0 ; n < objects.length ; n++)
        {
            if (n > 0)
                sb.append(separator);
            sb.append(String.valueOf(objects[n]));
        }
        
        return sb.toString();
    }
    
    public static String[] split(String str, char separatorChar)
    {
        if (str == null)
            return null;

        int len = str.length();
        if (len == 0)
            return new String[0];

        List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        boolean match = false;
        while (i < len)
        {
            if (str.charAt(i) == separatorChar)
            {
                if (match)
                {
                    list.add(str.substring(start, i));
                    match = false;
                }
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }
        if (match)
            list.add(str.substring(start, i));

        return list.toArray(new String[list.size()]);
    }
    
    /**
	 * Format the given size
	 */
	public static String formatSize( long size )
	{
		if (size == 0)
			return "0";
		
		StringBuilder sb = new StringBuilder();
		if (size < 0)
		{
			size = -size;
			sb.append("-");
		}
	
		long gigs = size / (1024*1024*1024);
		if (gigs > 0)
		{
			sb.append(new DecimalFormat("######.###").format((double)size/(1024*1024*1024)));
			sb.append(" GB");
			return sb.toString();
		}
		
		long megs = size / (1024*1024);
		if (megs > 0)
		{
			sb.append(new DecimalFormat("###.#").format((double)size/(1024*1024)));
			sb.append(" MB");
			return sb.toString();
		}
		
		long kbs = size / (1024);
		if (kbs > 0)
		{
			sb.append(new DecimalFormat("###.#").format((double)size/(1024)));
			sb.append(" KB");
			return sb.toString();
		}
		
		sb.append(size);
		sb.append(" B");
		
		return sb.toString();
	}
	
	private static final char[] HEX_CHARS = {'0', '1', '2', '3',
        '4', '5', '6', '7',
        '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f',};

	public static String asHex(byte[] data)
	{
		if (data == null || data.length == 0)
			return "";
		
		char buf[] = new char[data.length * 2];
		for (int i = 0, x = 0; i < data.length; i++)
		{
			buf[x++] = HEX_CHARS[(data[i] >>> 4) & 0xf];
			buf[x++] = HEX_CHARS[data[i] & 0xf];
		}
		return new String(buf);
	}
}
