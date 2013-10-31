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
package net.timewalker.ffmq4.common.message.selector.expression.utils;

import java.lang.reflect.Array;
import java.util.StringTokenizer;

import javax.jms.InvalidSelectorException;

/**
 * StringUtils
 */
public final class StringUtils
{    
	/**
	 * Replace double single quotes in the target string
	 */
	public static String replaceDoubleSingleQuotes( String value )
	{
		int idx = value.indexOf("''");
		if (idx == -1)
			return value;
		
		int len = value.length();
		int pos = 0;
		StringBuffer sb = new StringBuffer(len);
		while (idx != -1)
		{
			if (idx > pos)
			{
				sb.append(value.substring(pos, idx));
				pos += (idx-pos);
			}
			
			sb.append("'");	
			pos+=2;
			
			idx = value.indexOf("''",pos);
		}
		if (pos < len)
			sb.append(value.substring(pos, len));
			
		return sb.toString();
	}
	
	/**
	 * Replace single quotes in the target string by double single quotes
	 */
	public static String replaceSingleQuotes( String value )
	{
		int idx = value.indexOf("'");
		if (idx == -1)
			return value;
		
		int len = value.length();
		int pos = 0;
		StringBuffer sb = new StringBuffer(len);
		while (idx != -1)
		{
			if (idx > pos)
			{
				sb.append(value.substring(pos, idx));
				pos += (idx-pos);
			}
			
			sb.append("''");	
			pos++;
			
			idx = value.indexOf("'",pos);
		}
		if (pos < len)
			sb.append(value.substring(pos, len));
			
		return sb.toString();
	}
	
    /**
     * Parse a number as string
     */
    public static Number parseNumber( String numberAsString )  throws InvalidSelectorException
    {
        if (numberAsString == null) return null;
        try
        {
            if (numberAsString.indexOf('.') != -1 || 
                numberAsString.indexOf('e') != -1 ||
                numberAsString.indexOf('E') != -1)
            {
                return new Double(numberAsString);
            }
            else
                return Long.valueOf(numberAsString);
        }
        catch (NumberFormatException e)
        {
            throw new InvalidSelectorException("Invalid numeric value : "+numberAsString);
        }
    }
    
    /**
     * Check if the given text matches a pattern
     */
    public static Boolean matches( String text , String pattern , String escapeChar )
    {
        if (pattern.length() == 0)
            return text.length() == 0 ? Boolean.TRUE : Boolean.FALSE;
        
        int textPos = 0;
        boolean startWithAny = false;
        String separators = "%_";
        if (escapeChar != null)
            separators += escapeChar;
        StringTokenizer st = new StringTokenizer(pattern,separators,true);
        boolean escapeNext = false;
        while (st.hasMoreTokens())
        {
            String subPattern = st.nextToken();
            
            if (escapeChar != null && !escapeNext && subPattern.equals(escapeChar))
            {
                escapeNext = true;
            }
            else
            if (!escapeNext && subPattern.equals("%"))
            {
                startWithAny = true;
            }
            else
            if (!escapeNext && subPattern.equals("_"))
            {
                if (textPos < text.length())
                    textPos++;
                else
                    return Boolean.FALSE;
            }
            else
            {
                if (startWithAny)
                {
                    int matchPos = text.lastIndexOf(subPattern);
                    if (matchPos == -1 || matchPos < textPos) 
                        return Boolean.FALSE;
                    textPos = matchPos + subPattern.length();
                    startWithAny = false;
                }
                else
                {
                    if (!text.startsWith(subPattern,textPos))
                        return Boolean.FALSE;
                    else
                        textPos += subPattern.length();
                }
                escapeNext = false;
            }
        }
        
        return Boolean.TRUE;
    }
    
    public static String implode( Object array , String delimiter )
    {
    	StringBuffer sb = new StringBuffer();
    	int len = Array.getLength(array);
    	for (int i = 0; i < len; i++)
		{
    		if (i>0)
    			sb.append(delimiter);
			Object value = Array.get(array, i);
			sb.append(String.valueOf(value));
		}
    	return sb.toString();
    }
    
    public static String rightPad( String text , int len , char paddingChar )
    {
    	if (text.length() >= len)
    		return text;
    	
    	StringBuffer sb = new StringBuffer(len);
    	sb.append(text);
    	for(int n=0;n<(len-text.length());n++)
    		sb.append(paddingChar);
    	
    	return sb.toString();
    }
}
