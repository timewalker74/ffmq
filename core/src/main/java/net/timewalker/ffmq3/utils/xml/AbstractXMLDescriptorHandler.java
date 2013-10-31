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
package net.timewalker.ffmq3.utils.xml;

import java.util.Stack;

import net.timewalker.ffmq3.utils.StringTools;
import net.timewalker.ffmq3.utils.SystemTools;
import net.timewalker.ffmq3.utils.descriptor.AbstractDescriptor;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * AbstractXMLDescriptorHandler
 */
public abstract class AbstractXMLDescriptorHandler extends DefaultHandler
{
    private StringBuffer valueBuffer = new StringBuffer();
    private Stack nameStack = new Stack();
    
    /**
     * Constructor
     */
    public AbstractXMLDescriptorHandler()
    {
        // Nothing
    }
    
    /**
     * Get the parsed descriptor
     */
    public abstract AbstractDescriptor getDescriptor();
    
    /**
     * Get the current element value
     */
    protected String getElementValue()
    {
        return SystemTools.replaceSystemProperties(valueBuffer.toString().trim());
    }
    
    /*
     * (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement( String uri , String localName , String qName , Attributes attributes ) throws SAXException
    {
        super.startElement(uri,localName,qName,attributes);
        valueBuffer.setLength(0); // Clear value buffer
        nameStack.push(qName);
        String currentPath = StringTools.join(nameStack, "/");
        before(qName,currentPath,attributes);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String name) throws SAXException
    {
        super.endElement(uri, localName, name);
        String currentPath = StringTools.join(nameStack, "/");
        onNode(name,currentPath);
        nameStack.pop();
     }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    public void characters( char[] ch , int start , int length ) throws SAXException
    {
        valueBuffer.append(ch,start,length);
    }
    
    /**
     * Node pre-treatment
     */
    protected abstract void before( String qName , String currentPath , Attributes attributes ) throws SAXException;
    
    /**
     * Node treatment
     */
    protected abstract void onNode( String qName , String currentPath ) throws SAXException;
    
    protected String getRequired( Attributes attributes , String attributeName ) throws SAXException
    {
        String value = SystemTools.replaceSystemProperties(attributes.getValue(attributeName));
        if (value == null)
            throw new SAXException("Missing required attribute : "+attributeName);
        return value;
    }
}
