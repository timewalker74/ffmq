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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.jms.JMSException;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.utils.StringTools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 *  Template mapping provider.
 *  Allows the mapping of destinations names to actual definitions.
 * </p>
 */
public final class TemplateMappingProvider
{
    private static final Log log = LogFactory.getLog(TemplateMappingProvider.class);
    
    private FFMQEngineSetup setup;
    
    private List queueMappings = new Vector();
    private List topicMappings = new Vector();
    
    /**
     * Constructor
     */
    public TemplateMappingProvider( FFMQEngineSetup setup )
    {
        this.setup = setup;
    }
    
    public void loadMappings() throws JMSException
    {
        File mappingFile = setup.getTemplateMappingFile();
        if (mappingFile == null)
        {
            log.debug("Templates mapping file not defined, skipping.");
            return;
        }
        
        try
        {
            BufferedReader input = new BufferedReader(new FileReader(mappingFile));
            
            String line;
            while ((line = input.readLine()) != null)
            {
                // Strip everything after the comment delimiter, if any
                int commentSepIdx = line.indexOf('#');
                if (commentSepIdx != -1)
                    line = line.substring(0,commentSepIdx);
                
                // Trim the remaining line
                line = line.trim();
                
                // Skip empty lines
                if (line.length() == 0)
                    continue;
                
                String[] tokens = StringTools.split(line, ':');
                if (tokens.length != 3)
                    throw new FFMQException("Invalid template mapping line : "+line,"INVALID_TEMPLATE_MAPPING");
                boolean isQueueTemplate = tokens[0].equalsIgnoreCase("queue");
                String destinationNamePattern = tokens[1];
                String templateName = tokens[2];
                
                if (isQueueTemplate)
                    addQueueTemplateMapping(new TemplateMapping(destinationNamePattern,templateName));
                else
                    addTopicTemplateMapping(new TemplateMapping(destinationNamePattern,templateName));
            }
            
            input.close();
        }
        catch (IOException e)
        {
            throw new FFMQException("Cannot read templates mapping file","FS_ERROR",e);
        }
    }
    
    public void addQueueTemplateMapping( TemplateMapping mappingEntry )
    {
        queueMappings.add(mappingEntry);
    }
    
    public void addTopicTemplateMapping( TemplateMapping mappingEntry )
    {
        topicMappings.add(mappingEntry);
    }
    
    public String getTemplateNameForQueue( String queueName )
    {
        for (int i = 0 ; i < queueMappings.size() ; i++)
        {
            TemplateMapping mapping = (TemplateMapping)queueMappings.get(i);
            if (StringTools.matches(queueName,mapping.getPattern()))
            {
                log.debug("Matching template for queue "+queueName+" : "+mapping.getTemplateName());
                return mapping.getTemplateName();
            }
        }
        log.debug("No matching template for queue "+queueName);
        return null;
    }
    
    public String getTemplateNameForTopic( String topicName )
    {
        for (int i = 0 ; i < topicMappings.size() ; i++)
        {
            TemplateMapping mapping = (TemplateMapping)topicMappings.get(i);
            if (StringTools.matches(topicName,mapping.getPattern()))
            {
                log.debug("Matching template for topic "+topicName+" : "+mapping.getTemplateName());
                return mapping.getTemplateName();
            }
        }
        log.debug("No matching template for topic "+topicName);
        return null;
    }
}
