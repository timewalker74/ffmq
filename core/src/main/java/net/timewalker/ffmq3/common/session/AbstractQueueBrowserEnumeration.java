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
package net.timewalker.ffmq3.common.session;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.utils.JavaTools;

/**
 * <p>Base implementation for a {@link QueueBrowser} enumeration.</p>
 */
public abstract class AbstractQueueBrowserEnumeration implements Enumeration
{
    // Unique ID
    protected String id;
    
    // Parent browser
    protected AbstractQueueBrowser browser;
    
    // Attributes
    protected Object closeLock = new Object();
    protected boolean closed;
    
    /**
	 * Constructor
	 */
	public AbstractQueueBrowserEnumeration( AbstractQueueBrowser browser , String enumId )
	{
		this.id = enumId;
		this.browser = browser;
	}
    
    /**
	 * @return the id
	 */
	public final String getId()
	{
		return id;
	}
	
	/**
	 * Check that the queue browser
	 * @throws JMSException
	 */
	public final void checkNotClosed() throws JMSException
	{
		if (closed)
			throw new FFMQException("Queue browser enumeration is closed","ENUMERATION_CLOSED");
	}
	
	/**
	 * Close the enumeration
	 * @throws JMSException on close failure
	 */
	public final void close()
	{
		synchronized (closeLock)
		{
			if (closed)
				return;
			closed = true;
			onQueueBrowserEnumerationClose();
		}
	}
	
	/**
	 * @throws JMSException  
	 */
	protected void onQueueBrowserEnumerationClose()
	{
		browser.unregisterEnumeration(this);
	}
	
	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append(JavaTools.getShortClassName(getClass()));
        sb.append("[#");
        sb.append(id);
        sb.append("]");
        
        return sb.toString();
    }
}
