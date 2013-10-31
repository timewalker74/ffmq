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
package net.timewalker.ffmq3.common.connection;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import net.timewalker.ffmq3.FFMQVersion;

/**
 * <p>FFMQ implementation of the ConnectionMetaData interface</p>
 * @see ConnectionMetaData
 */
public class ConnectionMetaDataImpl implements javax.jms.ConnectionMetaData
{
    /*
     * (non-Javadoc)
     * @see javax.jms.ConnectionMetaData#getJMSMajorVersion()
     */
    @Override
	public int getJMSMajorVersion()
    {
        return FFMQVersion.getJMSMajorVersion();
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.ConnectionMetaData#getJMSMinorVersion()
     */
    @Override
	public int getJMSMinorVersion()
    {
        return FFMQVersion.getJMSMinorVersion();
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.ConnectionMetaData#getJMSProviderName()
     */
    @Override
	public String getJMSProviderName()
    {
        return "FFMQ";
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.ConnectionMetaData#getJMSVersion()
     */
    @Override
	public String getJMSVersion()
    {
        return FFMQVersion.getJMSMajorVersion()+"."+FFMQVersion.getJMSMinorVersion();
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.ConnectionMetaData#getJMSXPropertyNames()
     */
    @Override
	public Enumeration getJMSXPropertyNames()
    {
        return new Enumeration<Object>() {
            /*
             * (non-Javadoc)
             * @see java.util.Enumeration#hasMoreElements()
             */
            @Override
			public boolean hasMoreElements()
            {
                return false;
            }

            /*
             * (non-Javadoc)
             * @see java.util.Enumeration#nextElement()
             */
            @Override
			public Object nextElement()
            {
                throw new NoSuchElementException();
            }
        };
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.ConnectionMetaData#getProviderMajorVersion()
     */
    @Override
	public int getProviderMajorVersion()
    {
        return FFMQVersion.getProviderMajorVersion();
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.ConnectionMetaData#getProviderMinorVersion()
     */
    @Override
	public int getProviderMinorVersion()
    {
        return FFMQVersion.getProviderMinorVersion();
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.ConnectionMetaData#getProviderVersion()
     */
    @Override
	public String getProviderVersion()
    {
        return FFMQVersion.getProviderMajorVersion()+"."+FFMQVersion.getProviderMinorVersion();
    }
}
