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
package net.timewalker.ffmq3.common.message.selector.expression.operator;


import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.Message;

import net.timewalker.ffmq3.common.message.selector.expression.ConditionalExpression;
import net.timewalker.ffmq3.common.message.selector.expression.Identifier;
import net.timewalker.ffmq3.common.message.selector.expression.SelectorNode;

/**
 * InOperator
 */
public class InOperator extends AbstractBinaryOperator implements ConditionalExpression
{
    /**
     * Constructor
     */
    public InOperator( SelectorNode leftOperand , SelectorNode rightOperand ) throws InvalidSelectorException
    {
        super(leftOperand,rightOperand);
        if (!(leftOperand instanceof Identifier))
    		throw new InvalidSelectorException("left operand of IN operator must be an identifier");
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.selector.expression.SelectorNode#evaluate(javax.jms.Message)
     */
    public Object evaluate(Message message) throws JMSException
    {
        String leftValue = leftOperand.evaluateString(message);
        String[] rightValue = (String[])rightOperand.evaluate(message);
        if (leftValue == null)
        	return null; // [JMS Spec]

        // Look for a match
        for (int n = 0 ; n < rightValue.length ; n++)
        	if (rightValue[n] != null && leftValue.equals(rightValue[n]))
        		return Boolean.TRUE;
        
        return Boolean.FALSE;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
    	return parenthesize(leftOperand)+" IN "+parenthesize(rightOperand);
    }
}
