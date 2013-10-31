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

import javax.jms.JMSException;
import javax.jms.Message;

import net.timewalker.ffmq3.common.message.selector.MessageSelectorParser;
import net.timewalker.ffmq3.common.message.selector.expression.SelectorNode;

/**
 * <p>Object implementation of a JMS message selector</p>
 */
public final class MessageSelector
{
    // Parsed selector node tree
    private SelectorNode selectorTree;
    
    /**
     * Constructor
     */
    public MessageSelector( String selectorString ) throws JMSException
    {
        this.selectorTree = new MessageSelectorParser(selectorString).parse();
    }

    /**
     * Test the selector against a given message
     */
    public boolean matches( Message message ) throws JMSException
    {
        Boolean result = selectorTree != null ? selectorTree.evaluateBoolean(message) : null;
        return result != null && result.booleanValue();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return selectorTree != null ? selectorTree.toString() : "";
    }
}
