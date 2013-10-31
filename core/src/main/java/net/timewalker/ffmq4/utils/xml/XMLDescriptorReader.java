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
package net.timewalker.ffmq4.utils.xml;

import java.io.File;
import java.io.FileInputStream;

import javax.jms.JMSException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.utils.descriptor.AbstractDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * XMLDescriptorReader
 */
public class XMLDescriptorReader
{
    private static final Log log = LogFactory.getLog(XMLDescriptorReader.class);
    
    /**
     * Constructor
     */
    public XMLDescriptorReader()
    {
        // Nothing
    }
    
    /**
     * Read and parse an XML descriptor file
     */
    public AbstractDescriptor read( File descriptorFile , Class<? extends AbstractXMLDescriptorHandler> handlerClass ) throws JMSException
    {
        if (!descriptorFile.canRead())
            throw new FFMQException("Can't read descriptor file : "+descriptorFile.getAbsolutePath(),"FS_ERROR");
        
        log.debug("Parsing descriptor : "+descriptorFile.getAbsolutePath());
        
        AbstractXMLDescriptorHandler handler;
        try
        {
            // Create an handler instance
            handler = handlerClass.newInstance();
            
            // Parse the descriptor file
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            FileInputStream in = new FileInputStream(descriptorFile);
            parser.parse(in,handler);
            in.close();
        }
        catch (Exception e)
        {
            throw new FFMQException("Cannot parse descriptor file : "+descriptorFile.getAbsolutePath(),"PARSE_ERROR",e);
        }
        
        AbstractDescriptor descriptor = handler.getDescriptor();
        descriptor.setDescriptorFile(descriptorFile);

        return descriptor;
    }
}
