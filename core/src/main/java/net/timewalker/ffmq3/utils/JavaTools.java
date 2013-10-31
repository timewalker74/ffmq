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
package net.timewalker.ffmq3.utils;

/**
 * JavaUtils
 */
public class JavaTools
{
    /**
     * Get the short name of the given class
     */
    public static String getShortClassName( Class<?> clazz )
    {
        String className = clazz.getName();
        int idx = className.lastIndexOf('.');
        return (idx == -1 ? className : className.substring(idx+1));
    }
    
    /**
     * Get the name of the caller method
     */
    public static String getCallerMethodName( int offset )
    {
        StackTraceElement[] stack = new Exception().getStackTrace();
        return stack[offset+1].getMethodName();
    }
}
