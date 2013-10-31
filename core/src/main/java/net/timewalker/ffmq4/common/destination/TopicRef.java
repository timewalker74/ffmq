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
package net.timewalker.ffmq3.common.destination;

import javax.jms.Topic;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import net.timewalker.ffmq3.jndi.JNDIObjectFactory;
import net.timewalker.ffmq3.security.Resource;


/**
 * <p>Implementation of a Topic reference (not a real topic)</p>
 * @see DestinationRef
 */
public class TopicRef extends DestinationRef implements Topic
{
	private static final long serialVersionUID = 1L;
	
	// Attributes
    protected String name;
    
    /**
     * Constructor
     */
    public TopicRef( String topicName )
    {
        this.name = topicName;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.Topic#getTopicName()
     */
    @Override
	public final String getTopicName()
    {
        return name;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.common.destination.DestinationRef#getResourceName()
     */
    @Override
	public final String getResourceName() 
    {
		return Resource.TOPIC_PREFIX+name;
	}
    
    /* (non-Javadoc)
     * @see javax.naming.Referenceable#getReference()
     */
    @Override
	public final Reference getReference() throws NamingException
    {
    	Reference ref = new Reference(getClass().getName(),JNDIObjectFactory.class.getName(),null);
    	ref.add(new StringRefAddr("topicName",name));
    	return ref;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
        return "Topic("+name+")";
    }
}
