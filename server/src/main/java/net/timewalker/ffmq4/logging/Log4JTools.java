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

package net.timewalker.ffmq4.logging;

import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Log4JTools
 */
public final class Log4JTools
{
	public static void initializeLog4J( Properties props )
    {
    	// Typical code : PropertyConfigurator.configure(props);
    	// Soft dependency -> use reflection to configure log4j
    	try
    	{
	    	Class<?> configuratorClass = Class.forName("org.apache.log4j.PropertyConfigurator");
	    	Method configureMethod = configuratorClass.getMethod("configure", new Class[] { Properties.class });
	    	configureMethod.invoke(null, new Object[] { props });
    	}
    	catch (ClassNotFoundException e)
    	{
    		// No log4j support, ignore ...
    	}
    	catch (Exception e)
    	{
    		System.err.println("Cannot configure log4j :");
    		System.err.println(e);
    	}
    }
}
