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

/**
 * ArithmeticUtils
 */
public final class ArithmeticUtils
{
    /**
     * Sum two numbers
     */
    public static Number sum( Number n1 , Number n2 )
    {
        Class<?> type = getComputationType(n1,n2);
        Number val1 = convertTo(n1,type);
        Number val2 = convertTo(n2,type);

        if (type == Long.class)
            return new Long(val1.longValue()+val2.longValue());
        
        return new Double(val1.doubleValue()+val2.doubleValue());
    }
    
    /**
     * Substract two numbers
     */
    public static Number substract( Number n1 , Number n2 )
    {
        Class<?> type = getComputationType(n1,n2);
        Number val1 = convertTo(n1,type);
        Number val2 = convertTo(n2,type);

        if (type == Long.class)
            return new Long(val1.longValue()-val2.longValue());
        
        return new Double(val1.doubleValue()-val2.doubleValue());
    }
    
    /**
     * Multiply two numbers
     */
    public static Number multiply( Number n1 , Number n2 )
    {
        Class<?> type = getComputationType(n1,n2);
        Number val1 = convertTo(n1,type);
        Number val2 = convertTo(n2,type);

        if (type == Long.class)
            return new Long(val1.longValue()*val2.longValue());
        
        return new Double(val1.doubleValue()*val2.doubleValue());
    }
    
    /**
     * Divide two numbers
     */
    public static Number divide( Number n1 , Number n2 )
    {
        Class<?> type = getComputationType(n1,n2);
        Number val1 = convertTo(n1,type);
        Number val2 = convertTo(n2,type);

        if (type == Long.class)
        {
            if (val2.longValue() == 0)
                return null;
            return new Long(val1.longValue()/val2.longValue());
        }
        
        if (val2.doubleValue() == 0)
            return null;
        return new Double(val1.doubleValue()/val2.doubleValue());
    }
    
    /**
     * Negate a number
     */
    public static Number minus( Number n )
    {
        Number value = normalize(n);
        Class<?> type = value.getClass();
        
        if (type == Long.class)
            return new Long(-n.longValue());
        
        return new Double(-n.doubleValue());
    }
    
    /**
     * Compare two numbers
     */
    public static Boolean greaterThan( Number n1 , Number n2 )
    {
        Class<?> type = getComputationType(n1,n2);
        Number val1 = convertTo(n1,type);
        Number val2 = convertTo(n2,type);

        if (type == Long.class)
            return val1.longValue() > val2.longValue() ? Boolean.TRUE : Boolean.FALSE;
        return val1.doubleValue() > val2.doubleValue() ? Boolean.TRUE : Boolean.FALSE;
    }
    
    /**
     * Compare two numbers
     */
    public static Boolean greaterThanOrEquals( Number n1 , Number n2 )
    {
        Class<?> type = getComputationType(n1,n2);
        Number val1 = convertTo(n1,type);
        Number val2 = convertTo(n2,type);

        if (type == Long.class)
            return val1.longValue() >= val2.longValue() ? Boolean.TRUE : Boolean.FALSE;
        return val1.doubleValue() >= val2.doubleValue() ? Boolean.TRUE : Boolean.FALSE;
    }
    
    /**
     * Compare two numbers
     */
    public static Boolean lessThan( Number n1 , Number n2 )
    {
        Class<?> type = getComputationType(n1,n2);
        Number val1 = convertTo(n1,type);
        Number val2 = convertTo(n2,type);

        if (type == Long.class)
            return val1.longValue() < val2.longValue() ? Boolean.TRUE : Boolean.FALSE;
        return val1.doubleValue() < val2.doubleValue() ? Boolean.TRUE : Boolean.FALSE;
    }
    
    /**
     * Compare two numbers
     */
    public static Boolean lessThanOrEquals( Number n1 , Number n2 )
    {
        Class<?> type = getComputationType(n1,n2);
        Number val1 = convertTo(n1,type);
        Number val2 = convertTo(n2,type);

        if (type == Long.class)
            return val1.longValue() <= val2.longValue() ? Boolean.TRUE : Boolean.FALSE;
        return val1.doubleValue() <= val2.doubleValue() ? Boolean.TRUE : Boolean.FALSE;
    }
    
    /**
     * Compare two numbers
     */
    public static Boolean equals( Number n1 , Number n2 )
    {
        Class<?> type = getComputationType(n1,n2);
        Number val1 = convertTo(n1,type);
        Number val2 = convertTo(n2,type);

        if (type == Long.class)
            return val1.longValue() == val2.longValue() ? Boolean.TRUE : Boolean.FALSE;
        return val1.doubleValue() == val2.doubleValue() ? Boolean.TRUE : Boolean.FALSE;
    }
    
    private static Number convertTo( Number n , Class<?> type )
    {
        Class<?> baseType = n.getClass();
        if (baseType == type)
            return n;

        if (type == Long.class)
            return new Long(n.longValue());
        return new Double(n.doubleValue());
    }
    
    public static Number normalize( Number value )
    {
        Class<?> type = value.getClass();
        if (type == Long.class || type == Double.class)
            return value;
        
        if (isIntegerType(type))
            return new Long(value.longValue());
        else
            return new Double(value.doubleValue());
    }
    
    private static boolean isIntegerType( Class<?> type )
    {
        return type == Byte.class ||
               type == Short.class ||
               type == Integer.class ||
               type == Long.class;
    }
    
    private static Class<?> getComputationType( Number n1 , Number n2 )
    {
        Class<?> type1 = n1.getClass();
        Class<?> type2 = n2.getClass();
        if (isIntegerType(type1) && isIntegerType(type2))
            return Long.class;
        else
            return Double.class;
    }
}
