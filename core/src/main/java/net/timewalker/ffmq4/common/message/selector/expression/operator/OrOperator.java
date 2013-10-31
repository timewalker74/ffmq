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

import net.timewalker.ffmq3.common.message.selector.expression.SelectorNode;

/**
 * OrOperatorSelectorNode
 * 
 * | OR   |   T   |   F   |   U
 * +------+-------+-------+--------
 * |  T   |   T   |   T   |   T
 * |  F   |   T   |   F   |   U
 * |  U   |   T   |   U   |   U
 * +------+-------+-------+------- 
 */
public class OrOperator extends AbstractConditionalBinaryOperator
{
    /**
     * Constructor
     */
    public OrOperator( SelectorNode leftOperand , SelectorNode rightOperand ) throws InvalidSelectorException
    {
        super(leftOperand,rightOperand);
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.selector.expression.SelectorNode#evaluate(javax.jms.Message)
     */
    @Override
	public Object evaluate( Message message ) throws JMSException
    {
        Boolean leftOperandValue = leftOperand.evaluateBoolean(message);
        if (leftOperandValue == null)
        {
            Boolean rightOperandValue = rightOperand.evaluateBoolean(message);
            if (rightOperandValue == null || !rightOperandValue.booleanValue())
                return null;
            
            return Boolean.TRUE;
        }
        else
        {
            if (leftOperandValue.booleanValue())
                return Boolean.TRUE;
            
            return rightOperand.evaluateBoolean(message);
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
    	return parenthesize(leftOperand)+" OR "+parenthesize(rightOperand);
    }
}
