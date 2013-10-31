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
package net.timewalker.ffmq3.management.destination;

import javax.jms.JMSException;

import net.timewalker.ffmq3.management.InvalidDescriptorException;
import net.timewalker.ffmq3.utils.Checkable;

/**
 * <p>Implementation of a destination reference descriptor.</p>
 */
public final class DestinationReferenceDescriptor implements Checkable
{
	private String destinationType;
	private String destinationName;
	
	public String getDestinationType()
	{
		return destinationType;
	}
	
	public void setDestinationType(String destinationType)
	{
		this.destinationType = destinationType;
	}
	
	public String getDestinationName()
	{
		return destinationName;
	}
	
	public void setDestinationName(String destinationName)
	{
		this.destinationName = destinationName;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.utils.Checkable#check()
	 */
	@Override
	public void check() throws JMSException
	{
		if (destinationType == null)
			throw new InvalidDescriptorException("Missing destination reference property : 'destinationType'");
		if (!destinationType.equals("queue") && !destinationType.equals("topic"))
			throw new InvalidDescriptorException("Destination reference property 'destinationType' should be one of : queue, topic");
		if (destinationName == null)
			throw new InvalidDescriptorException("Missing destination reference property : 'destinationName'");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
        
        sb.append("destinationType=");
        sb.append(destinationType);
        sb.append(" destinationName=");
        sb.append(destinationName);
        
        return sb.toString();
	}
}
