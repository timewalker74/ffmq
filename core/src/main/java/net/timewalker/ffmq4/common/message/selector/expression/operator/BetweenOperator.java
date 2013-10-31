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

import net.timewalker.ffmq3.common.message.selector.expression.ArithmeticExpression;
import net.timewalker.ffmq3.common.message.selector.expression.ConditionalExpression;
import net.timewalker.ffmq3.common.message.selector.expression.SelectorNode;
import net.timewalker.ffmq3.common.message.selector.expression.utils.ArithmeticUtils;

/**
 * BetweenOperator
 */
public class BetweenOperator extends SelectorNode implements ConditionalExpression
{
    protected SelectorNode leftOperand;
    protected SelectorNode lowerBoundOperand;
    protected SelectorNode upperBoundOperand;
    
    /**
     * Constructor
     */
    public BetweenOperator( SelectorNode leftOperand,
                            SelectorNode lowerBoundOperand,
                            SelectorNode upperBoundOperand ) throws InvalidSelectorException
    {
        super();
        this.leftOperand = leftOperand;
        this.lowerBoundOperand = lowerBoundOperand;
        this.upperBoundOperand = upperBoundOperand;
        
        if (!(leftOperand instanceof ArithmeticExpression))
    		throw new InvalidSelectorException("left operand of BETWEEN operator must be an arithmetic expression");
        if (!(lowerBoundOperand instanceof ArithmeticExpression))
        	throw new InvalidSelectorException("lower bound of BETWEEN operator must be an arithmetic expression");
        if (!(upperBoundOperand instanceof ArithmeticExpression))
        	throw new InvalidSelectorException("upper bound of BETWEEN operator must be an arithmetic expression");
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.selector.expression.SelectorNode#evaluate(javax.jms.Message)
     */
    @Override
	public Object evaluate(Message message) throws JMSException
    {
        Number value = leftOperand.evaluateNumeric(message);
        Number lowerBound = lowerBoundOperand.evaluateNumeric(message);
        Number upperBound = upperBoundOperand.evaluateNumeric(message);
        if (value == null || lowerBound == null || upperBound == null)
            return null;
        
        return ArithmeticUtils.greaterThanOrEquals(value, lowerBound).booleanValue() &&
               ArithmeticUtils.lessThanOrEquals(value, upperBound).booleanValue() ?
               Boolean.TRUE : Boolean.FALSE;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
        return parenthesize(leftOperand)+" BETWEEN "+parenthesize(lowerBoundOperand)+" AND "+parenthesize(upperBoundOperand);
    }
}
