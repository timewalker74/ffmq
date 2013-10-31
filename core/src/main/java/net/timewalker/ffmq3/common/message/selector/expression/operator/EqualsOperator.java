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


import javax.jms.JMSException;
import javax.jms.Message;

import net.timewalker.ffmq3.common.message.selector.expression.SelectorNode;
import net.timewalker.ffmq3.common.message.selector.expression.SelectorNodeType;
import net.timewalker.ffmq3.common.message.selector.expression.utils.ArithmeticUtils;

/**
 * EqualsOperator
 */
public class EqualsOperator extends AbstractComparisonOperator
{
    /**
     * Constructor
     */
    public EqualsOperator( SelectorNode leftOperand , SelectorNode rightOperand )
    {
        super(leftOperand,rightOperand);
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.selector.expression.SelectorNode#evaluate(javax.jms.Message)
     */
    @Override
	public Object evaluate(Message message) throws JMSException
    {
        Object leftValue = leftOperand.evaluate(message);
        Object rightValue = rightOperand.evaluate(message);
        if (leftValue == null || rightValue == null)
            return null;
        
        int leftType = getNodeType(leftValue);
        int rightType = getNodeType(rightValue);
        if (leftType != rightType)
            return Boolean.FALSE;
        
        switch (leftType)
        {
        	case SelectorNodeType.STRING :
        	case SelectorNodeType.BOOLEAN :
        		return leftValue.equals(rightValue) ? Boolean.TRUE : Boolean.FALSE;
        	default :
        		return ArithmeticUtils.equals((Number)leftValue,(Number)rightValue);
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
        return parenthesize(leftOperand)+" = "+parenthesize(rightOperand);
    }
}
