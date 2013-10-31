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
package net.timewalker.ffmq3.common.message.selector.expression;


import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * <p>Implementation for a message selector language Identifier node.</p>
 */
public final class Identifier extends SelectorNode implements AtomicOperand, ConditionalExpression, ArithmeticExpression
{
    // Attributes
    private String name;
    
    /**
     * Constructor
     */
    public Identifier( String name )
    {
        super();
        this.name = name;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.selector.expression.SelectorNode#evaluate(javax.jms.Message)
     */
    @Override
	public Object evaluate( Message message ) throws JMSException
    {
        // Standard headers
        if (name.equals("JMSCorrelationID"))
            return message.getJMSCorrelationID();
        if (name.equals("JMSMessageID"))
            return message.getJMSMessageID();
        if (name.equals("JMSType"))
            return message.getJMSType();
        if (name.equals("JMSDeliveryMode"))
            return message.getJMSDeliveryMode() == DeliveryMode.PERSISTENT ? "PERSISTENT" : "NON_PERSISTENT";
        if (name.equals("JMSPriority"))
            return new Integer(message.getJMSPriority());
        if (name.equals("JMSTimestamp"))
            return new Long(message.getJMSTimestamp());
                
        return message.getObjectProperty(name);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
        return name;
    }
}
