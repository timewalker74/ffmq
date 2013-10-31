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
package net.timewalker.ffmq3.management.destination.definition;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Topic;

import net.timewalker.ffmq3.FFMQSubscriberPolicy;
import net.timewalker.ffmq3.common.destination.DestinationTools;
import net.timewalker.ffmq3.management.InvalidDescriptorException;
import net.timewalker.ffmq3.utils.Settings;

/**
 * <p>Implementation of a {@link Topic} definition descriptor.</p>
 */
public final class TopicDefinition extends AbstractDestinationDefinition
{
	// Attributes
	private int subscriberFailurePolicy;
	private int subscriberOverflowPolicy;
		
    /**
     * Constructor
     */
    public TopicDefinition()
    {
        super();
    }
    
    /**
     * Constructor
     */
    public TopicDefinition( Settings settings )
    {
        super(settings);
    }
    
    /**
	 * @param subscriberFailurePolicy the subscriberFailurePolicy to set
	 */
	public void setSubscriberFailurePolicy(int subscriberFailurePolicy)
	{
		this.subscriberFailurePolicy = subscriberFailurePolicy;
	}
	
	/**
	 * @param subscriberOverflowPolicy the subscriberOverflowPolicy to set
	 */
	public void setSubscriberOverflowPolicy(int subscriberOverflowPolicy)
	{
		this.subscriberOverflowPolicy = subscriberOverflowPolicy;
	}
	
	/**
	 * @return the subscriberFailurePolicy
	 */
	public int getSubscriberFailurePolicy()
	{
		return subscriberFailurePolicy;
	}
	
	/**
	 * @return the subscriberOverflowPolicy
	 */
	public int getSubscriberOverflowPolicy()
	{
		return subscriberOverflowPolicy;
	}
    
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.management.destination.AbstractDestinationDescriptor#initFromSettings(net.timewalker.ffmq3.utils.Settings)
	 */
	protected void initFromSettings(Settings settings)
	{
		super.initFromSettings(settings);
		
		this.subscriberFailurePolicy  = settings.getIntProperty("subscriberFailurePolicy",FFMQSubscriberPolicy.SUBSCRIBER_POLICY_LOG);
		this.subscriberOverflowPolicy = settings.getIntProperty("subscriberOverflowPolicy",FFMQSubscriberPolicy.SUBSCRIBER_POLICY_LOG);
	}
	
    /**
     * Create a queue definition from this template
     */
    public QueueDefinition createQueueDefinition( String topicName , String consumerId , boolean temporary )
    {
        QueueDefinition def = new QueueDefinition();
        def.setName(DestinationTools.getQueueNameForTopicConsumer(topicName, consumerId));
        def.setTemporary(temporary);
        copyAttributesTo(def);
        
        return def;
    }
    
    /**
     * Test if this topic definition supports the given delivery mode
     * @param deliveryMode a delivery mode
     * @return true if the mode is supported
     */
    public boolean supportDeliveryMode( int deliveryMode )
    {
    	switch (deliveryMode)
    	{
    		case DeliveryMode.PERSISTENT :     return initialBlockCount > 0;
    		case DeliveryMode.NON_PERSISTENT : return maxNonPersistentMessages > 0;
    		default :
    			throw new IllegalArgumentException("Invalid delivery mode : "+deliveryMode);
    	}
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.management.destination.DestinationDescriptor#check()
     */
    public void check() throws JMSException
    {
        super.check();
        
        // Check queue name
        DestinationTools.checkTopicName(name);
        
        // Policies
        if (!FFMQSubscriberPolicy.isValid(subscriberFailurePolicy))
    		throw new InvalidDescriptorException("Invalid subscriber failure policy mask : "+subscriberFailurePolicy);
    	if (!FFMQSubscriberPolicy.isValid(subscriberOverflowPolicy))
    		throw new InvalidDescriptorException("Invalid subscriber overflow policy mask : "+subscriberOverflowPolicy);
    }
}
