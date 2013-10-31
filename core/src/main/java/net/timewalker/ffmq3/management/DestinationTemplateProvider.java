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
package net.timewalker.ffmq3.management;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

import javax.jms.JMSException;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.management.destination.template.QueueTemplate;
import net.timewalker.ffmq3.management.destination.template.TopicTemplate;
import net.timewalker.ffmq3.utils.Settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DestinationTemplateProvider
 */
public final class DestinationTemplateProvider extends AbstractDefinitionProvider
{
    private static final Log log = LogFactory.getLog(DestinationTemplateProvider.class);
    
    private Map queueTemplates = new Hashtable();
    private Map topicTemplates = new Hashtable();
    
    /**
     * Constructor
     */
    public DestinationTemplateProvider( FFMQEngineSetup setup )
    {
        super(setup);
    }
    
    public void loadExistingTemplates() throws JMSException
    {
        File templatesDir = setup.getTemplatesDir();
        if (templatesDir != null)
        {
            log.info("Loading existing templates from : "+templatesDir.getAbsolutePath());
            
            File[] queueTemplateDescriptors = DescriptorTools.getDescriptorFiles(templatesDir,"queueTemplate-",".properties");
            if (queueTemplateDescriptors != null)
            {
                for (int i = 0 ; i < queueTemplateDescriptors.length ; i++)
                {
                    QueueTemplate queueTemplate = loadQueueTemplate(queueTemplateDescriptors[i]);
                    addQueueTemplate(queueTemplate);
                }
            }
            log.debug("Loaded "+queueTemplates.size()+" queue templates");
            
            File[] topicTemplateDescriptors = DescriptorTools.getDescriptorFiles(templatesDir,"topicTemplate-",".properties");
            if (topicTemplateDescriptors != null)
            {
                for (int i = 0 ; i < topicTemplateDescriptors.length ; i++)
                {
                    TopicTemplate topicTemplate = loadTopicTemplate(topicTemplateDescriptors[i]);
                    addTopicTemplate(topicTemplate);
                }
            }
            log.debug("Loaded "+topicTemplates.size()+" topic templates");
        }
    }
    
    public void addQueueTemplate( QueueTemplate queueTemplate ) throws JMSException
    {
        // Check template consistency
        try
        {
            queueTemplate.check();
        }
        catch (InvalidDescriptorException e)
        {
            throw new FFMQException("Cannot register queue template : "+queueTemplate,"INVALID_QUEUE_TEMPLATE",e);
        }
        
        if (queueTemplates.put(queueTemplate.getName(), queueTemplate) != null)
            throw new FFMQException("Queue template name already used : "+queueTemplate.getName(),"DUPLICATE_QUEUE_TEMPLATE");
    }
    
    public void addTopicTemplate( TopicTemplate topicTemplate ) throws JMSException
    {
        // Check template consistency
        try
        {
            topicTemplate.check();
        }
        catch (InvalidDescriptorException e)
        {
            throw new FFMQException("Cannot register topic template : "+topicTemplate,"INVALID_TOPIC_TEMPLATE",e);
        }
        
        if (topicTemplates.put(topicTemplate.getName(), topicTemplate) != null)
            throw new FFMQException("Topic template name already used : "+topicTemplate.getName(),"DUPLICATE_TOPIC_TEMPLATE");
    }
    
    public QueueTemplate getQueueTemplate( String queueName ) throws JMSException
    {
        QueueTemplate queueDef = (QueueTemplate)queueTemplates.get(queueName);
        if (queueDef == null)
        {
            queueDef = loadQueueTemplate(queueName);
            if (queueDef == null)
                return null;
                
            queueTemplates.put(queueName, queueDef);
        }  
        return queueDef;
    }
    
    private QueueTemplate loadQueueTemplate( String queueName ) throws JMSException
    {
        if (setup.getTemplatesDir() == null)
            return null;
        
        return loadQueueTemplate(new File(setup.getTemplatesDir(),"queue-"+queueName+".properties"));
    }
    
    private QueueTemplate loadQueueTemplate( File queueTemplateDescriptor ) throws JMSException
    {
        if (!queueTemplateDescriptor.exists())
            return null;
        
        if (!queueTemplateDescriptor.canRead())
            throw new FFMQException("Cannot access queue template descriptor : "+queueTemplateDescriptor.getAbsolutePath(),"FS_ERROR");
        
        Settings queueSettings = new Settings();
        queueSettings.readFrom(queueTemplateDescriptor);
        
        return new QueueTemplate(queueSettings); 
    }

    public TopicTemplate getTopicTemplate( String topicName ) throws JMSException
    {
        TopicTemplate topicDef = (TopicTemplate)topicTemplates.get(topicName);
        if (topicDef == null)  
        {
            topicDef = loadTopicTemplate(topicName);
            if (topicDef == null)
                return null;
            
            topicTemplates.put(topicName, topicDef);
        }  
        return topicDef;
    }
    
    private TopicTemplate loadTopicTemplate( String topicName ) throws JMSException
    {
        if (setup.getTemplatesDir() == null)
            return null;
        
        return loadTopicTemplate(new File(setup.getTemplatesDir(),"topic-"+topicName+".properties"));
    }
    
    private TopicTemplate loadTopicTemplate( File topicTemplateDescriptor ) throws JMSException
    {
        if (!topicTemplateDescriptor.exists())
            return null;
            
        if (!topicTemplateDescriptor.canRead())
            throw new FFMQException("Cannot access topic template descriptor : "+topicTemplateDescriptor.getAbsolutePath(),"FS_ERROR");
        
        Settings topicSettings = new Settings();
        topicSettings.readFrom(topicTemplateDescriptor);
        
        return new TopicTemplate(topicSettings);
    }
    
    public void clear()
    {
        queueTemplates.clear();
        topicTemplates.clear();
    }
}
