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
package net.timewalker.ffmq3.security;

import java.lang.reflect.InvocationTargetException;

import javax.jms.JMSException;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.management.FFMQEngineSetup;
import net.timewalker.ffmq3.utils.Settings;

/**
 * SecurityConnectorProvider
 */
public final class SecurityConnectorProvider
{
    private static SecurityConnector connector;
    
    /**
     * Get the security connector instance
     */
    public static synchronized SecurityConnector getConnector( FFMQEngineSetup setup ) throws JMSException
    {
        if (connector == null)
        {
            String connectorType = setup.getSecurityConnectorType();
            try
            {
                Class<?> connectorClass = Class.forName(connectorType);
                connector = (SecurityConnector)connectorClass
                    .getConstructor(new Class[] { Settings.class })
                    .newInstance(new Object[] { setup.getSettings() });
            }
            catch (ClassNotFoundException e)
            {
                throw new FFMQException("Security connector class not found : "+connectorType,"SECURITY_ERROR",e);
            }
            catch (ClassCastException e)
            {
                throw new FFMQException("Invalid security connector class : "+connectorType,"SECURITY_ERROR",e);
            }
            catch (InstantiationException e)
            {
                throw new FFMQException("Cannot create security connector","SECURITY_ERROR",e.getCause());
            }
            catch (InvocationTargetException e)
            {
                throw new FFMQException("Cannot create security connector","SECURITY_ERROR",e.getTargetException());
            }
            catch (Exception e)
            {
                throw new FFMQException("Cannot create security connector","SECURITY_ERROR",e);
            }
        }
        return connector;
    }
}
