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

/**
 * SystemTools
 */
public class SystemTools
{
	/**
	 * Replace system properties in the given string value
	 * @param value a string value
	 * @return the expanded value
	 */
	public static String replaceSystemProperties( String value )
	{
    	// Dumb case
    	if (value == null || value.length() == 0)
    		return value;
    	
		StringBuffer sb = new StringBuffer();

		int pos = 0;
		int start;
		
		while ((start = value.indexOf("${",pos)) != -1)
		{
			if (start > pos)
				sb.append(value.substring(pos,start));
			
			int end = value.indexOf('}',start+2);
			if (end == -1)
			{
				pos = start;
				break;
			}
			
			String varName = value.substring(start+2,end);
			String varValue = System.getProperty(varName,"${"+varName+"}");
			sb.append(varValue);
			
			pos = end+1;
		}
		
		// Append remaining
		if (pos < value.length())
			sb.append(value.substring(pos));

		return sb.toString();
	}
}
