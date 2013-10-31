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
package net.timewalker.ffmq4.management;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.jms.JMSException;

import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.management.bridge.BridgeDefinition;
import net.timewalker.ffmq4.management.bridge.handler.BridgeDescriptorHandler;
import net.timewalker.ffmq4.utils.xml.XMLDescriptorReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * BridgeDefinitionProvider
 */
public final class BridgeDefinitionProvider
{
	private static final Log log = LogFactory.getLog(BridgeDefinitionProvider.class);
	
	private File bridgeDefinitionsDir;
	private List<BridgeDefinition> bridgeDefinitionList = new Vector<>();
	private Set<String> bridgeNames = new HashSet<>(); 
	
	/**
	 * Constructor
	 */
	public BridgeDefinitionProvider( File bridgeDefinitionsDir )
	{
		this.bridgeDefinitionsDir = bridgeDefinitionsDir;
	}
	
	/**
	 * @return the bridgeDefinitionList
	 */
	public BridgeDefinition[] getBridgeDefinitions()
	{
		return bridgeDefinitionList.toArray(new BridgeDefinition[bridgeDefinitionList.size()]);
	}
	
	public void loadExistingDefinitions() throws JMSException
    {
        log.info("Loading existing bridge definitions from : "+bridgeDefinitionsDir.getAbsolutePath());
        File[] bridgeDescriptors = DescriptorTools.getDescriptorFiles(bridgeDefinitionsDir,"bridge-",".xml");
        if (bridgeDescriptors != null)
        {
            for (int i = 0 ; i < bridgeDescriptors.length ; i++)
            {
                BridgeDefinition bridgeDef = loadBridgeDefinition(bridgeDescriptors[i]);
                if (bridgeDef != null)
                    addBridgeDefinition(bridgeDef);
            }
        }
        log.debug("Loaded "+bridgeDefinitionList.size()+" bridge definitions");
    }
    
	public void addBridgeDefinition( BridgeDefinition bridgeDef ) throws JMSException
	{
	    bridgeDef.check();
	    
	    if (!bridgeNames.add(bridgeDef.getName()))
	        throw new FFMQException("Bridge name already exists : "+bridgeDef.getName(), "BRIDGE_ALREADY_EXIST");
	        
	    bridgeDefinitionList.add(bridgeDef);
	}
	
    private BridgeDefinition loadBridgeDefinition( File bridgeDescriptor ) throws JMSException
    {
        if (!bridgeDescriptor.exists())
            return null;
        
        if (!bridgeDescriptor.canRead())
            throw new FFMQException("Cannot access bridge definition descriptor : "+bridgeDescriptor.getAbsolutePath(),"FS_ERROR");
        
        return (BridgeDefinition)new XMLDescriptorReader().read(bridgeDescriptor, BridgeDescriptorHandler.class);
    }
}
