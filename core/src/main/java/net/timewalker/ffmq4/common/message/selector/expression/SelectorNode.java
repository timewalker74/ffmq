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
package net.timewalker.ffmq4.common.message.selector.expression;

import javax.jms.JMSException;
import javax.jms.Message;

import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.common.message.selector.expression.utils.ArithmeticUtils;

/**
 * <p>Base implementation for a message selector language node.</p>
 */
public abstract class SelectorNode
{
    /**
     * Evaluate the node
     */
    public abstract Object evaluate( Message message ) throws JMSException;
    
    /**
     * Negate a boolean value
     */
    protected final Object negate( Object value )
    {
        if (value == null)
            return null;
        
        return ((Boolean)value).booleanValue() ? Boolean.FALSE : Boolean.TRUE;
    }
    
    /**
     * Evaluate this node as a boolean
     */
    public final Boolean evaluateBoolean( Message message ) throws JMSException
    {
        Object value = evaluate(message);
        if (value == null)
            return null;
        
        if (value instanceof Boolean)
            return (Boolean)value;
        
        throw new FFMQException("Expected a boolean but got : "+value.toString(),"INVALID_SELECTOR_EXPRESSION");
    }
    
    /**
     * Evaluate this node as a number
     */
    public final Number evaluateNumeric( Message message ) throws JMSException
    {
        Object value = evaluate(message);
        if (value == null)
            return null;
 
        if (value instanceof Number)
            return ArithmeticUtils.normalize((Number)value);
        
        throw new FFMQException("Expected a numeric but got : "+value.toString(),"INVALID_SELECTOR_EXPRESSION");
    }
    
    /**
     * Evaluate this node as a string
     */
    public final String evaluateString( Message message ) throws JMSException
    {
        Object value = evaluate(message);
        if (value == null)
            return null;
        
        if (value instanceof String)
            return (String)value;
        
        throw new FFMQException("Expected a string but got : "+value.toString(),"INVALID_SELECTOR_EXPRESSION");
    }
    
    /**
     * Get the type of a given value
     */
    protected final int getNodeType( Object value )
    {   	
        if (value instanceof String)
            return SelectorNodeType.STRING;
            
        if (value instanceof Boolean)
            return SelectorNodeType.BOOLEAN;
        
        return SelectorNodeType.NUMBER;
    }
    
    protected final String parenthesize( SelectorNode node )
    {
    	if (node instanceof AtomicOperand)
    		return node.toString();
    	else
    		return "("+node+")";
    }
}
