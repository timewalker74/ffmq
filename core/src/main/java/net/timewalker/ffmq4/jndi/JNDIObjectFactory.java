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
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

import net.timewalker.ffmq4.FFMQConstants;
import net.timewalker.ffmq4.common.destination.QueueRef;
import net.timewalker.ffmq4.common.destination.TopicRef;

/**
 * <p>Implementation of an {@link ObjectFactory} allowing to transport JNDI references of the following JMS objects :
 * <ul>
 *  <li>Queue
 *  <li>Topic
 *  <li>ConnectionFactory
 *  <li>QueueConnectionFactory
 *  <li>TopicConnectionFactory
 * </ul>
 * </p>
 */
public final class JNDIObjectFactory implements ObjectFactory
{
	/* (non-Javadoc)
	 * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object, javax.naming.Name, javax.naming.Context, java.util.Hashtable)
	 */
	@Override
	public Object getObjectInstance(Object obj, Name name, Context context, Hashtable<?,?> environment) throws Exception
	{
		if (!(obj instanceof Reference))
			throw new IllegalStateException("Object is not a reference : "+obj);
		
        Reference reference = (Reference)obj;
        String objClassName = reference.getClassName();
        
        // Queue
        if (objClassName.equals(QueueRef.class.getName()))
        {
        	String queueName = getRequiredAttribute(reference, "queueName");
        	return new QueueRef(queueName);
        }
        
        // Topic
        if (objClassName.equals(TopicRef.class.getName()))
        {
        	String topicName = getRequiredAttribute(reference, "topicName");
        	return new TopicRef(topicName);
        }
        
        // ConnectionFactory
        if (objClassName.equals(FFMQConnectionFactory.class.getName()))
        	return new FFMQConnectionFactory(recreateConnectionFactoryEnv(reference));
        
        // QueueConnectionFactory
        if (objClassName.equals(FFMQQueueConnectionFactory.class.getName()))
        	return new FFMQQueueConnectionFactory(recreateConnectionFactoryEnv(reference));
        
        // TopicConnectionFactory
        if (objClassName.equals(FFMQTopicConnectionFactory.class.getName()))
        	return new FFMQTopicConnectionFactory(recreateConnectionFactoryEnv(reference));

        // We don't know how to handle this
        return null; 
	}
	
	private Hashtable<String,Object> recreateConnectionFactoryEnv( Reference reference )
	{
		Hashtable<String,Object> env = new Hashtable<>();
		
		// Provider URL
		String providerURL = getRequiredAttribute(reference, "providerURL");
    	env.put(Context.PROVIDER_URL,providerURL);
    	
    	// Client ID
    	String clientID = getAttribute(reference, "clientID");
    	if (clientID != null)
    		env.put(FFMQConstants.JNDI_ENV_CLIENT_ID,clientID);
    	
    	// Username / password
    	String userName = getAttribute(reference, "userName");
    	if (userName != null)
    	{
    		env.put(Context.SECURITY_PRINCIPAL,userName);
    		
    		String password = getAttribute(reference, "password");
        	if (password != null)
        		env.put(Context.SECURITY_CREDENTIALS,password);
    	}
    	
    	return env;
	}
	
	private String getRequiredAttribute( Reference ref , String attributeName )
	{
		StringRefAddr attrRef = (StringRefAddr)ref.get(attributeName);
		if (attrRef == null)
    		throw new IllegalArgumentException("Missing resource attribute : "+attributeName);
		return (String)attrRef.getContent();
	}
	
	private String getAttribute( Reference ref , String attributeName )
	{
		StringRefAddr attrRef = (StringRefAddr)ref.get(attributeName);
		if (attrRef == null)
    		return null;
		return (String)attrRef.getContent();
	}
}
