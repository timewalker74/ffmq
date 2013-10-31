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
package net.timewalker.ffmq3.management.bridge.handler;

import net.timewalker.ffmq3.management.bridge.BridgeDefinition;
import net.timewalker.ffmq3.utils.descriptor.AbstractDescriptor;
import net.timewalker.ffmq3.utils.xml.AbstractXMLDescriptorHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * <p>XML de-serialization handler for a {@link BridgeDefinition} descriptor.</p> 
 */
public final class BridgeDescriptorHandler extends AbstractXMLDescriptorHandler
{
	private BridgeDefinition bridgeDefinition = new BridgeDefinition();
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.utils.xml.DescriptorHandler#getDescriptor()
	 */
	@Override
	public AbstractDescriptor getDescriptor()
	{
		return bridgeDefinition;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.utils.xml.DescriptorHandler#before(java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	protected void before(String name, String currentPath, Attributes attributes) throws SAXException
	{
		// Nothing
	}
	
	/*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.xml.DescriptorHandler#onNode(java.lang.String, java.lang.String)
     */
    @Override
	protected void onNode(String name, String currentPath) throws SAXException
    {
        if (currentPath.equals("bridgeDefinition"))
        {
            // Nothing
        }
        else if (currentPath.equals("bridgeDefinition/name"))
        {
            bridgeDefinition.setName(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/enabled"))
        {
            bridgeDefinition.setEnabled(getElementValue().equalsIgnoreCase("true"));
        }
        else if (currentPath.equals("bridgeDefinition/retryInterval"))
        {
            bridgeDefinition.setRetryInterval(Integer.parseInt(getElementValue()));
        }
        else if (currentPath.equals("bridgeDefinition/commitSourceFirst"))
        {
            bridgeDefinition.setCommitSourceFirst(getElementValue().equalsIgnoreCase("true"));
        }
        else if (currentPath.equals("bridgeDefinition/producerTransacted"))
        {
            bridgeDefinition.setProducerTransacted(getElementValue().equalsIgnoreCase("true"));
        }
        else if (currentPath.equals("bridgeDefinition/consumerTransacted"))
        {
            bridgeDefinition.setConsumerTransacted(getElementValue().equalsIgnoreCase("true"));
        }
        else if (currentPath.equals("bridgeDefinition/consumerAcknowledgeMode"))
        {
            bridgeDefinition.setConsumerAcknowledgeMode(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/producerDeliveryMode"))
        {
            bridgeDefinition.setProducerDeliveryMode(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/source"))
        {
            // Nothing
        }
        else if (currentPath.equals("bridgeDefinition/source/peer"))
        {
            // Nothing
        }
        else if (currentPath.equals("bridgeDefinition/source/peer/jdniInitialContextFactoryName"))
        {
            bridgeDefinition.getSource().setJdniInitialContextFactoryName(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/source/peer/jndiConnectionFactoryName"))
        {
            bridgeDefinition.getSource().setJndiConnectionFactoryName(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/source/peer/providerURL"))
        {
            bridgeDefinition.getSource().setProviderURL(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/source/peer/userName"))
        {
            bridgeDefinition.getSource().setUserName(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/source/peer/password"))
        {
            bridgeDefinition.getSource().setPassword(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/source/destination"))
        {
            // Nothing
        }
        else if (currentPath.equals("bridgeDefinition/source/destination/destinationType"))
        {
            bridgeDefinition.getSourceDestination().setDestinationType(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/source/destination/destinationName"))
        {
            bridgeDefinition.getSourceDestination().setDestinationName(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/target"))
        {
            // Nothing
        }
        else if (currentPath.equals("bridgeDefinition/target/peer"))
        {
            // Nothing
        }
        else if (currentPath.equals("bridgeDefinition/target/peer/jdniInitialContextFactoryName"))
        {
            bridgeDefinition.getTarget().setJdniInitialContextFactoryName(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/target/peer/jndiConnectionFactoryName"))
        {
            bridgeDefinition.getTarget().setJndiConnectionFactoryName(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/target/peer/providerURL"))
        {
            bridgeDefinition.getTarget().setProviderURL(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/target/peer/userName"))
        {
            bridgeDefinition.getTarget().setUserName(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/target/peer/password"))
        {
            bridgeDefinition.getTarget().setPassword(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/target/destination"))
        {
            // Nothing
        }
        else if (currentPath.equals("bridgeDefinition/target/destination/destinationType"))
        {
            bridgeDefinition.getTargetDestination().setDestinationType(getElementValue());
        }
        else if (currentPath.equals("bridgeDefinition/target/destination/destinationName"))
        {
            bridgeDefinition.getTargetDestination().setDestinationName(getElementValue());
        }
        else
            throw new SAXException("Unexpected node : " + name + " (" + currentPath + ")");
    }
    
    
}
