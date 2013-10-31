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

package net.timewalker.ffmq4.cluster.resolver;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.NamingException;

import net.timewalker.ffmq4.management.destination.DestinationReferenceDescriptor;
import net.timewalker.ffmq4.management.peer.PeerDescriptor;
import net.timewalker.ffmq4.utils.JNDITools;

/**
 * <p>
 * JNDI based implementation of a DestinationResolver
 * </p>
 * @see DestinationResolver
 */
public class JndiDestinationResolver implements DestinationResolver
{
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq4.cluster.resolver.DestinationResolver#getDestination(net.timewalker.ffmq4.management.peer.PeerDescriptor, net.timewalker.ffmq4.management.destination.DestinationReferenceDescriptor, javax.jms.Session)
	 */
	@Override
	public Destination getDestination(PeerDescriptor peer, DestinationReferenceDescriptor destinationReference, Session session) throws JMSException
	{
		try
		{
			Context jndiContext = JNDITools.getContext(peer.getJdniInitialContextFactoryName(),
													   peer.getProviderURL(),
													   null);
			
		    return (Destination)jndiContext.lookup(destinationReference.getDestinationName());
		}
		catch (NamingException e)
		{
			throw new JMSException("Cannot resolve destination in JNDI : "+e.toString());
		}
	}
}
