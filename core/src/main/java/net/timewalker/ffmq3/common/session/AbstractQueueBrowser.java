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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.utils.JavaTools;
import net.timewalker.ffmq3.utils.id.IntegerID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Base implementation for a {@link QueueBrowser}</p>
 */
public abstract class AbstractQueueBrowser implements QueueBrowser
{
	private static final Log log = LogFactory.getLog(AbstractQueueBrowser.class);
	
    // Unique ID
    protected IntegerID id;
    
    // Attributes
	protected Queue queue;
	protected String messageSelector;
	
	// Runtime
	protected Object closeLock = new Object();
	protected boolean closed;
	
	// Parent session
	protected AbstractSession session;
	
	// Children
	private Map enumMap = new Hashtable();
	
	/**
	 * Constructor
	 */
	public AbstractQueueBrowser( AbstractSession session , Queue queue , String messageSelector , IntegerID browserId )
	{
		this.session = session;
		this.queue = queue;
		this.messageSelector = messageSelector;
		this.id = browserId;
	}

	/**
	 * @return the id
	 */
	public final IntegerID getId()
	{
		return id;
	}

	/**
     * Register an enumeration
     */
    protected final void registerEnumeration( AbstractQueueBrowserEnumeration queueBrowserEnum )
    {
    	enumMap.put(queueBrowserEnum.getId(),queueBrowserEnum);
    }
    
    /**
     * Unregister an enumeration
     */
    protected final void unregisterEnumeration( AbstractQueueBrowserEnumeration queueBrowserEnum )
    {
    	enumMap.put(queueBrowserEnum.getId(),queueBrowserEnum);
    }
	
    /**
     * Lookup a registered enumeration
     */
    public final AbstractQueueBrowserEnumeration lookupRegisteredEnumeration( String enumId )
    {
        return (AbstractQueueBrowserEnumeration)enumMap.get(enumId);
    }
    
    /**
     * Close remaining browser enumerations
     */
    private void closeRemainingEnumerations()
    {
        List enumsToClose = new Vector();
        synchronized (enumMap)
        {
        	enumsToClose.addAll(enumMap.values());
            for (int n = 0 ; n < enumsToClose.size() ; n++)
            {
            	AbstractQueueBrowserEnumeration queueBrowserEnum = (AbstractQueueBrowserEnumeration)enumsToClose.get(n);
                log.debug("Auto-closing unclosed queue browser enumeration : "+queueBrowserEnum);
                try
                {
                	queueBrowserEnum.close();
                }
                catch (Exception e)
                {
                    log.error("Could not close queue browser enumeration "+queueBrowserEnum,e);
                }
            }
        }
    }
    
	/*
	 * (non-Javadoc)
	 * @see javax.jms.QueueBrowser#getMessageSelector()
	 */
	public final String getMessageSelector() throws JMSException
	{
		return messageSelector;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.jms.QueueBrowser#getQueue()
	 */
	public final Queue getQueue() throws JMSException
	{
		return queue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.jms.QueueBrowser#close()
	 */
	public final void close() throws JMSException
	{
		synchronized (closeLock)
		{
			if (closed)
				return;
			this.closed = true;
			onQueueBrowserClose();
		}
	}

	/**
	 * @throws JMSException  
	 */
	protected void onQueueBrowserClose()
	{
		session.unregisterBrowser(this);
		closeRemainingEnumerations();
	}
	
	/**
	 * Check that the queue browser
	 * @throws JMSException
	 */
	public final void checkNotClosed() throws JMSException
	{
		if (closed)
			throw new FFMQException("Queue browser is closed","QUEUE_BROWSER_CLOSED");
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
        sb.append("] queue=");
        sb.append(queue);
        sb.append("] messageSelector=");
        sb.append(messageSelector);
        
        return sb.toString();
    }
}
