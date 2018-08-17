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
package net.timewalker.ffmq4.management.destination.template;

import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.jms.Topic;

import net.timewalker.ffmq4.FFMQSubscriberPolicy;
import net.timewalker.ffmq4.common.message.selector.expression.utils.StringUtils;
import net.timewalker.ffmq4.management.InvalidDescriptorException;
import net.timewalker.ffmq4.management.destination.definition.TopicDefinition;
import net.timewalker.ffmq4.utils.Settings;

/**
 * <p>Implementation of a {@link Topic} template descriptor.</p>
 */
public final class TopicTemplate extends AbstractDestinationTemplate
{
	// Attributes
	private int subscriberFailurePolicy;
	private int subscriberOverflowPolicy;
	private String[] partitionsKeysToIndex;
	
    /**
     * Constructor
     */
    public TopicTemplate()
    {
        super();
    }
    
    /**
     * Constructor
     */
    public TopicTemplate( Settings settings )
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
	
	/**
	 * @return the partitionsKeysToIndex
	 */
	public String[] getPartitionsKeysToIndex()
	{
		return partitionsKeysToIndex;
	}
	
	/**
	 * @param partitionsKeysToIndex the partitionsKeysToIndex to set
	 */
	public void setPartitionsKeysToIndex(String[] partitionsKeysToIndex)
	{
		this.partitionsKeysToIndex = partitionsKeysToIndex;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq4.management.destination.AbstractDestinationDescriptor#initFromSettings(net.timewalker.ffmq4.utils.Settings)
	 */
	@Override
	protected void initFromSettings(Settings settings)
	{
		super.initFromSettings(settings);
		
		this.subscriberFailurePolicy  = settings.getIntProperty("subscriberFailurePolicy",FFMQSubscriberPolicy.SUBSCRIBER_POLICY_LOG);
		this.subscriberOverflowPolicy = settings.getIntProperty("subscriberOverflowPolicy",FFMQSubscriberPolicy.SUBSCRIBER_POLICY_LOG);
		
		String rawPartitionsKeysToIndex = settings.getStringProperty("partitionsKeysToIndex");
		if (rawPartitionsKeysToIndex != null)
		{
			StringTokenizer st = new StringTokenizer(rawPartitionsKeysToIndex, ", ");
			this.partitionsKeysToIndex = new String[st.countTokens()];
			int pos = 0;
			while (st.hasMoreTokens())
				this.partitionsKeysToIndex[pos++] = st.nextToken();
		}
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq4.management.destination.AbstractDestinationDescriptor#fillSettings(net.timewalker.ffmq4.utils.Settings)
	 */
	@Override
	protected void fillSettings(Settings settings)
	{
		super.fillSettings(settings);
		
		settings.setIntProperty("subscriberFailurePolicy", subscriberFailurePolicy);
		settings.setIntProperty("subscriberOverflowPolicy", subscriberOverflowPolicy);
		if (partitionsKeysToIndex != null)
			settings.setStringProperty("partitionsKeysToIndex", StringUtils.implode(partitionsKeysToIndex, ","));
	}
	
    /**
     * Create a topic definition from this template
     */
    public TopicDefinition createTopicDefinition( String topicName , boolean temporary )
    {
        TopicDefinition def = new TopicDefinition();
        def.setName(topicName);
        def.setTemporary(temporary);
        copyAttributesTo(def);
        def.setSubscriberFailurePolicy(subscriberFailurePolicy);
        def.setSubscriberOverflowPolicy(subscriberOverflowPolicy);
        def.setPartitionsKeysToIndex(partitionsKeysToIndex);
        
        return def;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.management.destination.AbstractDestinationDescriptor#check()
     */
    @Override
	public void check() throws JMSException
    {
    	super.check();
    	
    	// Policies
        if (!FFMQSubscriberPolicy.isValid(subscriberFailurePolicy))
    		throw new InvalidDescriptorException("Invalid subscriber failure policy mask : "+subscriberFailurePolicy);
    	if (!FFMQSubscriberPolicy.isValid(subscriberOverflowPolicy))
    		throw new InvalidDescriptorException("Invalid subscriber overflow policy mask : "+subscriberOverflowPolicy);
    	
    	// Partition keys
    	if (partitionsKeysToIndex != null)
    	{
    		if (partitionsKeysToIndex.length == 0)
    			throw new InvalidDescriptorException("Empty partitionsKeysToIndex definition");
    		
    		for(String key : partitionsKeysToIndex)
    		{
    			if (key.startsWith("JMS") && !key.equals("JMSCorrelationID"))
    				throw new InvalidDescriptorException("JMSCorrelationID is the only JMS standard header that may be indexed, cannot use "+key);
    		}
    	}
    }
}
