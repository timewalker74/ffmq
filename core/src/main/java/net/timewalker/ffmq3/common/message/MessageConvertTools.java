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
package net.timewalker.ffmq3.common.message;

import javax.jms.MessageFormatException;

import net.timewalker.ffmq3.utils.ArrayTools;

/**
 * Supported conversions (from the JMS 1.1 StreamMessage spec.)
 * 
 * (A value written as the row type can be read as the column type)
 * |        | boolean byte short char int long float double String byte[]
 * |----------------------------------------------------------------------
 * |boolean |    X                                            X
 * |byte    |          X     X         X   X                  X   
 * |short   |                X         X   X                  X   
 * |char    |                     X                           X
 * |int     |                          X   X                  X   
 * |long    |                              X                  X   
 * |float   |                                    X     X      X   
 * |double  |                                          X      X   
 * |String  |    X     X     X         X   X     X     X      X   
 * |byte[]  |                                                        X
 * |----------------------------------------------------------------------
 */
public final class MessageConvertTools
{
    public static boolean asBoolean( Object value ) throws MessageFormatException
    {
        if (value == null)
        	return false; // Boolean.valueOf((String)null).booleanValue();
            
        if (value instanceof Boolean)
            return ((Boolean)value).booleanValue();
        if (value instanceof String)
            return Boolean.valueOf((String)value).booleanValue();
        
        throw new MessageFormatException("Could not convert type to boolean : ("+value.getClass().getName()+") "+value);
    }
    
    public static byte asByte( Object value ) throws MessageFormatException
    {
        if (value == null)
            return Byte.valueOf((String)null).byteValue();
        
        if (value instanceof Byte)
            return ((Byte)value).byteValue();
        if (value instanceof String)
            return Byte.valueOf((String)value).byteValue();
        
        throw new MessageFormatException("Could not convert type to byte : ("+value.getClass().getName()+") "+value);
    }
    
    public static byte[] asBytes( Object value ) throws MessageFormatException
    {
        if (value == null)
            return null;
        
        if (value instanceof byte[])
        {
            return ArrayTools.copy((byte[])value);
        }
        
        throw new MessageFormatException("Could not convert type to byte[] : ("+value.getClass().getName()+") "+value);
    }
    
    public static short asShort( Object value ) throws MessageFormatException
    {
        if (value == null)
            return Short.valueOf((String)null).shortValue();
        
        if (value instanceof Short)
            return ((Short)value).shortValue();
        if (value instanceof Byte)
            return ((Byte)value).byteValue();
        if (value instanceof String)
            return Short.valueOf((String)value).shortValue();
        
        throw new MessageFormatException("Could not convert type to short : ("+value.getClass().getName()+") "+value);
    }
    
    public static int asInt( Object value ) throws MessageFormatException
    {
        if (value == null)
            return Integer.valueOf((String)null).intValue();
        
        if (value instanceof Integer)
            return ((Integer)value).intValue();
        if (value instanceof Byte)
            return ((Byte)value).byteValue();
        if (value instanceof Short)
            return ((Short)value).shortValue();
        if (value instanceof String)
           return Integer.valueOf((String)value).intValue();
        
        throw new MessageFormatException("Could not convert type to int : ("+value.getClass().getName()+") "+value);
    }
    
    public static long asLong( Object value ) throws MessageFormatException
    {
        if (value == null)
            return Long.valueOf((String)null).longValue();
        
        if (value instanceof Long)
            return ((Long)value).longValue();
        if (value instanceof Byte)
            return ((Byte)value).byteValue();
        if (value instanceof Short)
            return ((Short)value).shortValue();
        if (value instanceof Integer)
            return ((Integer)value).intValue();
        if (value instanceof String)
            return Long.valueOf((String)value).longValue();
        
        throw new MessageFormatException("Could not convert type to long : ("+value.getClass().getName()+") "+value);
    }
    
    public static float asFloat( Object value ) throws MessageFormatException
    {
        if (value == null)
            throw new NullPointerException();
            // Same as "return Float.valueOf((String)null).floatValue();" (JMS Spec)
        
        if (value instanceof Float)
            return ((Float)value).floatValue();
        if (value instanceof String)
            return Float.valueOf((String)value).floatValue();
        
        throw new MessageFormatException("Could not convert type to float : ("+value.getClass().getName()+") "+value);
    }
    
    public static double asDouble( Object value ) throws MessageFormatException
    {
        if (value == null)
            throw new NullPointerException();
            // Same as "return Double.valueOf((String)null).doubleValue();" (JMS Spec)
        
        if (value instanceof Double)
            return ((Double)value).doubleValue();
        if (value instanceof Float)
            return ((Float)value).floatValue();
        if (value instanceof String)
            return Double.valueOf((String)value).doubleValue();
        
        throw new MessageFormatException("Could not convert type to double : ("+value.getClass().getName()+") "+value);
    }
    
    public static char asChar( Object value ) throws MessageFormatException
    {
        if (value == null)
            throw new NullPointerException(); // [JMS Spec]
        
        if (value instanceof Character)
            return ((Character)value).charValue();
        
        throw new MessageFormatException("Could not convert type to char : ("+value.getClass().getName()+") "+value);
    }
    
    public static String asString( Object value ) throws MessageFormatException
    {
        if (value == null)
            return null;
        
        if (value instanceof String)
            return (String)value;
        
        if (value instanceof Boolean ||
            value instanceof Byte ||
            value instanceof Character ||
            value instanceof Short ||
            value instanceof Integer ||
            value instanceof Long ||
            value instanceof Float ||
            value instanceof Double ||
            value instanceof Character)
            return String.valueOf(value);
            
        throw new MessageFormatException("Could not convert type to String : ("+value.getClass().getName()+") "+value);
    }
}
