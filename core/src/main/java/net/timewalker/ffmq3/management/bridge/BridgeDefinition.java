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
package net.timewalker.ffmq3.management.bridge;

import javax.jms.JMSException;

import net.timewalker.ffmq3.cluster.bridge.JMSBridgeDefinitionMBean;
import net.timewalker.ffmq3.management.InvalidDescriptorException;
import net.timewalker.ffmq3.management.ManagementUtils;
import net.timewalker.ffmq3.management.destination.DestinationReferenceDescriptor;
import net.timewalker.ffmq3.management.peer.PeerDescriptor;
import net.timewalker.ffmq3.utils.descriptor.AbstractXMLBasedDescriptor;

/**
 * <p>
 *  Named descriptor for a JMS Bridge.
 *  <ul>
 *  <li>Source and target queuers of the bridge are described through PeerDescriptor descriptors.
 *  <li>Source and target destinations of the bridge are described through DestinationReferenceDescriptor descriptors.
 *  </ul>
 * </p>
 * @see PeerDescriptor
 */
public final class BridgeDefinition extends AbstractXMLBasedDescriptor implements JMSBridgeDefinitionMBean
{
    // Attributes
	private String name;
	private Boolean enabled;
	private Integer retryInterval;
	private Boolean commitSourceFirst;
	private Boolean producerTransacted;
	private Boolean consumerTransacted;
	private int consumerAcknowledgeMode = -1;
	private int producerDeliveryMode = -1;
	private PeerDescriptor source = new PeerDescriptor();
	private PeerDescriptor target = new PeerDescriptor();
	private DestinationReferenceDescriptor sourceDestination = new DestinationReferenceDescriptor();
	private DestinationReferenceDescriptor targetDestination = new DestinationReferenceDescriptor();
	
	/**
     * Constructor
     */
    public BridgeDefinition()
    {
        super();
    }
    
	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = Boolean.valueOf(enabled);
	}
	
	/**
	 * @return the enabled
	 */
	public boolean isEnabled()
	{
		return enabled != null ? enabled.booleanValue() : false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeDefinitionMBean#getRetryInterval()
	 */
	public int getRetryInterval()
	{
		return retryInterval != null ? retryInterval.intValue() : 0;
	}

	public void setRetryInterval(int retryInterval)
	{
		this.retryInterval = new Integer(retryInterval);
	}

	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeDefinitionMBean#isCommitSourceFirst()
	 */
	public boolean isCommitSourceFirst()
	{
		return commitSourceFirst != null ? commitSourceFirst.booleanValue() : false;
	}

	public void setCommitSourceFirst(boolean commitSourceFirst)
	{
		this.commitSourceFirst = Boolean.valueOf(commitSourceFirst);
	}

	/**
	 * @return the source
	 */
	public PeerDescriptor getSource()
	{
		return source;
	}
	
	/**
	 * @return the target
	 */
	public PeerDescriptor getTarget()
	{
		return target;
	}
	
	/**
	 * @return the sourceDestination
	 */
	public DestinationReferenceDescriptor getSourceDestination()
	{
		return sourceDestination;
	}
	
	/**
	 * @return the targetDestination
	 */
	public DestinationReferenceDescriptor getTargetDestination()
	{
		return targetDestination;
	}

	/**
	 * @param producerTransacted the producerTransacted to set
	 */
	public void setProducerTransacted(boolean producerTransacted)
	{
		this.producerTransacted = Boolean.valueOf(producerTransacted);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeDefinitionMBean#isProducerTransacted()
	 */
	public boolean isProducerTransacted()
	{
		return producerTransacted != null ? producerTransacted.booleanValue() : false;
	}
	
	/**
	 * @param consumerTransacted the consumerTransacted to set
	 */
	public void setConsumerTransacted(boolean consumerTransacted)
	{
		this.consumerTransacted = Boolean.valueOf(consumerTransacted);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeDefinitionMBean#isConsumerTransacted()
	 */
	public boolean isConsumerTransacted()
	{
		return consumerTransacted != null ? consumerTransacted.booleanValue() : false;
	}
	
	/**
	 * @param consumerAcknowledgeMode the consumerAcknowledgeMode to set
	 */
	public void setConsumerAcknowledgeMode(int consumerAcknowledgeMode)
	{
		this.consumerAcknowledgeMode = consumerAcknowledgeMode;
	}
	
	/**
	 * @param consumerAcknowledgeMode the consumerAcknowledgeMode to set
	 */
	public void setConsumerAcknowledgeMode(String consumerAcknowledgeMode)
	{
		this.consumerAcknowledgeMode = ManagementUtils.parseAcknowledgeMode(consumerAcknowledgeMode);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeDefinitionMBean#getConsumerAcknowledgeMode()
	 */
	public int getConsumerAcknowledgeMode()
	{
		return consumerAcknowledgeMode;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.timewalker.ffmq3.cluster.bridge.JMSBridgeDefinitionMBean#getProducerDeliveryMode()
	 */
	public int getProducerDeliveryMode()
	{
		return producerDeliveryMode;
	}
	
	/**
	 * @param producerDeliveryMode the producerDeliveryMode to set
	 */
	public void setProducerDeliveryMode(int producerDeliveryMode)
	{
		this.producerDeliveryMode = producerDeliveryMode;
	}
	
	/**
	 * @param producerDeliveryMode the producerDeliveryMode to set
	 */
	public void setProducerDeliveryMode(String producerDeliveryMode)
	{
		this.producerDeliveryMode = ManagementUtils.parseDeliveryMode(producerDeliveryMode);
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.utils.xml.Descriptor#check()
	 */
	public void check() throws JMSException
	{
		if (name == null)
			throw new InvalidDescriptorException("Missing bridge property : 'name'");
		if (enabled == null)
			throw new InvalidDescriptorException("Missing bridge property : 'enabled'");
		if (retryInterval == null)
			throw new InvalidDescriptorException("Missing bridge property : 'retryInterval'");
		if (getRetryInterval() < 0)
			throw new InvalidDescriptorException("Bridge property 'retryInterval' should be >= 0");
		if (commitSourceFirst == null)
			throw new InvalidDescriptorException("Missing bridge property : 'commitSourceFirst'");
		if (producerTransacted == null)
			throw new InvalidDescriptorException("Missing bridge property : 'producerTransacted'");
		if (consumerTransacted == null)
			throw new InvalidDescriptorException("Missing bridge property : 'consumerTransacted'");
		if (!isConsumerTransacted())
		{
			if (consumerAcknowledgeMode == -1)
				throw new InvalidDescriptorException("Missing bridge property : 'consumerAcknowledgeMode'");
		}
		if (producerDeliveryMode == -1)
			throw new InvalidDescriptorException("Missing bridge property : 'producerDeliveryMode'");
		
		source.check();
		target.check();
		sourceDestination.check();
		targetDestination.check();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
        
        sb.append("[");
        sb.append(name);
        sb.append("] retryInterval=");
        sb.append(retryInterval);
        sb.append(" commitSourceFirst=");
        sb.append(commitSourceFirst);
        sb.append(" producerTransacted=");
        sb.append(producerTransacted);
        sb.append(" consumerTransacted=");
        sb.append(consumerTransacted);
        sb.append(" consumerAcknowledgeMode=");
        sb.append(consumerAcknowledgeMode);
        sb.append(" producerDeliveryMode=");
        sb.append(producerDeliveryMode);
        sb.append(" source=(");
        sb.append(source);
        sb.append(") target=(");
        sb.append(target);
        sb.append(") sourceDestination=(");
        sb.append(sourceDestination);
        sb.append(") targetDestination=(");
        sb.append(targetDestination);
        sb.append(")");
        
        return sb.toString();
	}
}
