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
package net.timewalker.ffmq3.jmx;

import javax.jms.JMSException;
import javax.management.ObjectName;

/**
 * JMXAgent
 * <p>Allows to dynamically register/unregister objects in the local JMX server</p>
 */
public interface JMXAgent
{
    // Constants
    public static final String JMX_DOMAIN = "FFMQ";
    
    /**
	 * Register an MBean
	 * @throws JMSException on registration error
	 */
	public void register( ObjectName name , Object mBean ) throws JMSException;
  
  	/**
	 * Unregister an MBean
	 * @throws JMSException on registration error
	 */
	public void unregister( ObjectName name ) throws JMSException;
	
	/**
     * Stop the agent
     */
    public void stop();
}
