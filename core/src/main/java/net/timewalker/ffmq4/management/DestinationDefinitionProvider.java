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
import java.util.Hashtable;
import java.util.Map;

import javax.jms.JMSException;

import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq4.management.destination.definition.TopicDefinition;
import net.timewalker.ffmq4.utils.Settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DestinationDefinitionProvider
 */
public final class DestinationDefinitionProvider extends AbstractDefinitionProvider
{
    private static final Log log = LogFactory.getLog(DestinationDefinitionProvider.class);
    
    private Map<String,QueueDefinition> queueDefinitions = new Hashtable<>();
    private Map<String,TopicDefinition> topicDefinitions = new Hashtable<>();
     
    /**
     * Constructor
     */
    public DestinationDefinitionProvider( FFMQEngineSetup setup )
    {
        super(setup);
    }
    
    public void loadExistingDefinitions() throws JMSException
    {
        File definitionDir = setup.getDestinationDefinitionsDir();
        if (definitionDir != null)
        {
            log.info("Loading existing destinations definitions from : "+definitionDir.getAbsolutePath());
            
            File[] queueDescriptors = DescriptorTools.getDescriptorFiles(definitionDir,"queue-",".properties");
            if (queueDescriptors != null)
            {
                for (int i = 0 ; i < queueDescriptors.length ; i++)
                {
                    QueueDefinition queueDef = loadQueueDefinition(queueDescriptors[i]);
                    if (queueDef != null)
                    	queueDefinitions.put(queueDef.getName(), queueDef);
                }
            }
            log.debug("Loaded "+queueDefinitions.size()+" queue definitions");
            
            File[] topicDescriptors = DescriptorTools.getDescriptorFiles(definitionDir,"topic-",".properties");
            if (topicDescriptors != null)
            {
                for (int i = 0 ; i < topicDescriptors.length ; i++)
                {
                    TopicDefinition topicDef = loadTopicDefinition(topicDescriptors[i]);
                    topicDefinitions.put(topicDef.getName(), topicDef);
                }
            }
            log.debug("Loaded "+topicDefinitions.size()+" topic definitions");
        }
    }
    
    public QueueDefinition getQueueDefinition( String queueName ) throws JMSException
    {
        QueueDefinition queueDef = queueDefinitions.get(queueName);
        if (queueDef == null)  
        {
            queueDef = loadQueueDefinition(queueName);
            if (queueDef == null)
                return null;
                
            queueDefinitions.put(queueName, queueDef);
        }  
        return queueDef;
    }
    
    public boolean hasQueueDefinition( String queueName ) throws JMSException
    {
    	return getQueueDefinition(queueName) != null;
    }
    
    private QueueDefinition loadQueueDefinition( String queueName ) throws JMSException
    {
        if (setup.getDestinationDefinitionsDir() == null)
            return null;
        
        return loadQueueDefinition(new File(setup.getDestinationDefinitionsDir(),"queue-"+queueName+".properties"));
    }
    
    private QueueDefinition loadQueueDefinition( File queueDescriptor ) throws JMSException
    {
        if (!queueDescriptor.exists())
            return null;
        
        if (!queueDescriptor.canRead())
            throw new FFMQException("Cannot access queue definition descriptor : "+queueDescriptor.getAbsolutePath(),"FS_ERROR");
        
        Settings queueSettings = new Settings();
        queueSettings.readFrom(queueDescriptor);
        
        return new QueueDefinition(queueSettings);
    }
    
    public void addQueueDefinition( QueueDefinition queueDef ) throws JMSException
    {
        if (queueDefinitions.containsKey(queueDef.getName()))
            throw new FFMQException("Queue definition already exists : "+queueDef.getName(),"QUEUE_DEFINITION_ALREADY_EXIST");
        
        if (queueDef.hasDescriptor() && setup.getDestinationDefinitionsDir() != null)
        {
            // Check that the descriptor file does not exist
            File queueDescriptor = new File(setup.getDestinationDefinitionsDir(),"queue-"+queueDef.getName()+".properties");
            if (queueDescriptor.exists())
                throw new FFMQException("Queue descriptor already exists : "+queueDescriptor.getAbsolutePath(),"FS_ERROR");
            
            // Create the descriptor file
            log.debug("Persisting queue definition for "+queueDef.getName());
            Settings queueSettings = queueDef.asSettings();
            queueSettings.writeTo(queueDescriptor, "Queue definition descriptor for "+queueDef.getName());
        }
        
        // Register it
        queueDefinitions.put(queueDef.getName(), queueDef);
    }
    
    public void removeQueueDefinition( QueueDefinition queueDef )
    {
    	// if queue has a descriptor, delete it
    	if (queueDef.hasDescriptor() && setup.getDestinationDefinitionsDir() != null)
    	{
	        File queueDescriptor = new File(setup.getDestinationDefinitionsDir(),"queue-"+queueDef.getName()+".properties");
	        if (queueDescriptor.exists())
	            if (!queueDescriptor.delete())
	                log.error("Cannot delete queue descriptor file : "+queueDescriptor.getAbsolutePath());
    	}
        queueDefinitions.remove(queueDef.getName());
    }

    public TopicDefinition getTopicDefinition( String topicName ) throws JMSException
    {
        TopicDefinition topicDef = topicDefinitions.get(topicName);
        if (topicDef == null)  
        {
            topicDef = loadTopicDefinition(topicName);
            if (topicDef == null)
                return null;
            
            topicDefinitions.put(topicName, topicDef);
        }  
        return topicDef;
    }
    
    public boolean hasTopicDefinition( String queueName ) throws JMSException
    {
    	return getTopicDefinition(queueName) != null;
    }
    
    private TopicDefinition loadTopicDefinition( String topicName ) throws JMSException
    {
        if (setup.getDestinationDefinitionsDir() == null)
            return null;
        
        return loadTopicDefinition(new File(setup.getDestinationDefinitionsDir(),"topic-"+topicName+".properties"));
    }
    
    private TopicDefinition loadTopicDefinition( File topicDescriptor ) throws JMSException
    {
        if (!topicDescriptor.exists())
            return null;
            
        if (!topicDescriptor.canRead())
            throw new FFMQException("Cannot access topic definition descriptor : "+topicDescriptor.getAbsolutePath(),"FS_ERROR");
        
        Settings topicSettings = new Settings();
        topicSettings.readFrom(topicDescriptor);

        return new TopicDefinition(topicSettings);
    }
    
    public void addTopicDefinition( TopicDefinition topicDef ) throws JMSException
    {
        if (topicDefinitions.containsKey(topicDef.getName()))
            throw new FFMQException("Topic definition already exists : "+topicDef.getName(),"TOPIC_DEFINITION_ALREADY_EXIST");
        
        if (setup.getDestinationDefinitionsDir() != null)
        {
            // Check that the descriptor file does not exist
            File topicDescriptor = new File(setup.getDestinationDefinitionsDir(),"topic-"+topicDef.getName()+".properties");
            if (topicDescriptor.exists())
                throw new FFMQException("Topic descriptor already exists : "+topicDescriptor.getAbsolutePath(),"FS_ERROR");
            
            // Create the descriptor file
            Settings topicSettings = topicDef.asSettings();
            topicSettings.writeTo(topicDescriptor, "Topic definition descriptor for "+topicDef.getName());
        }
        
        // Register it
        topicDefinitions.put(topicDef.getName(), topicDef);
    }
    
    public void removeTopicDefinition( String topicName )
    {
        if (setup.getDestinationDefinitionsDir() != null)
        {
            File topicDescriptor = new File(setup.getDestinationDefinitionsDir(),"topic-"+topicName+".properties");
            if (topicDescriptor.exists())
                if (!topicDescriptor.delete())
                    log.error("Cannot delete topic descriptor file : "+topicDescriptor.getAbsolutePath());
        }
        topicDefinitions.remove(topicName);
    }
    
    public String[] getAllQueueNames()
    {
        return queueDefinitions.keySet().toArray(new String[queueDefinitions.size()]);
    }
    
    public String[] getAllTopicNames()
    {
        return topicDefinitions.keySet().toArray(new String[topicDefinitions.size()]);
    }
}
