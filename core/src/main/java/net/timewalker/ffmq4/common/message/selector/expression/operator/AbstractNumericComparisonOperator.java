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

import net.timewalker.ffmq3.common.message.selector.expression.ArithmeticExpression;
import net.timewalker.ffmq3.common.message.selector.expression.SelectorNode;

/**
 * AbstractNumericComparisonOperator
 */
public abstract class AbstractNumericComparisonOperator extends AbstractComparisonOperator
{
	/**
     * Constructor
     */
    public AbstractNumericComparisonOperator( SelectorNode leftOperand , SelectorNode rightOperand ) throws InvalidSelectorException
    {
        super(leftOperand,rightOperand);
        if (!(leftOperand instanceof ArithmeticExpression))
        	throw new InvalidSelectorException("left operand of numeric comparator must be an arithmetic expression");
        if (!(rightOperand instanceof ArithmeticExpression))
        	throw new InvalidSelectorException("right operand of numeric comparator must be an arithmetic expression");
    }
}
