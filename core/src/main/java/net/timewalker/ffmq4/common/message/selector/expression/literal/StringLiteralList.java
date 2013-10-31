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
package net.timewalker.ffmq3.common.message.selector.expression.literal;

import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.Message;

import net.timewalker.ffmq3.common.message.selector.expression.AtomicOperand;
import net.timewalker.ffmq3.common.message.selector.expression.SelectorNode;

/**
 * LiteralList
 */
public final class StringLiteralList extends SelectorNode implements AtomicOperand
{
    private SelectorNode[] items;
    
    /**
     * Constructor
     */
    public StringLiteralList( SelectorNode[] items ) throws InvalidSelectorException
    {
        super();
        this.items = items;
        for (int i = 0; i < items.length; i++)
        	if (!(items[i] instanceof StringLiteral))
            	throw new InvalidSelectorException("Only string literals are allowed after IN operator");
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.message.selector.expression.SelectorNode#evaluate(javax.jms.Message)
     */
    @Override
	public Object evaluate(Message message) throws JMSException
    {
        String[] values = new String[items.length];
        for (int n = 0 ; n < items.length ; n++)
            values[n] = items[n].evaluateString(message);
        
        return values;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        for (int n = 0 ; n < items.length ; n++)
        {
            if (n>0)
                sb.append(",");
            sb.append(String.valueOf(items[n]));
        }
        sb.append(")");
        return sb.toString();
    }
}
