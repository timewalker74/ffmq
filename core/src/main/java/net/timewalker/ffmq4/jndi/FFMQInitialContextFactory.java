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
package net.timewalker.ffmq4.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import net.timewalker.ffmq4.FFMQConstants;

/**
 * <p>
 *  Implementation of a JNDI {@link InitialContextFactory} that creates an FFMQ
 *  JNDI context, providing lookup for FFMQ connection factories.
 * </p>
 */
public final class FFMQInitialContextFactory implements InitialContextFactory
{
    /**
     * Constructor
     */
    public FFMQInitialContextFactory()
    {
        super();
    }

    /* (non-Javadoc)
     * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
     */
    @Override
	public Context getInitialContext(Hashtable<?,?> environment) throws NamingException
    {
        FFMQJNDIContext context = new FFMQJNDIContext(environment);
        preLoad(context);
        return context;
    }
    
    /**
     * Preload the context with factories
     */
    @SuppressWarnings("unchecked")
    protected void preLoad(FFMQJNDIContext context) throws NamingException
    {
        context.bind(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME, new FFMQConnectionFactory((Hashtable<String, Object>) context.getEnvironment()));
        context.bind(FFMQConstants.JNDI_QUEUE_CONNECTION_FACTORY_NAME, new FFMQQueueConnectionFactory((Hashtable<String, Object>) context.getEnvironment()));
        context.bind(FFMQConstants.JNDI_TOPIC_CONNECTION_FACTORY_NAME, new FFMQTopicConnectionFactory((Hashtable<String, Object>) context.getEnvironment()));
    }
}
