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
package net.timewalker.ffmq4.client;

import java.io.InputStream;
import java.util.Properties;

import javax.jms.JMSException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.timewalker.ffmq4.FFMQClientSettings;
import net.timewalker.ffmq4.FFMQCoreSettings;
import net.timewalker.ffmq4.transport.PacketTransportException;
import net.timewalker.ffmq4.transport.tcp.nio.NIOTcpMultiplexer;
import net.timewalker.ffmq4.utils.Settings;
import net.timewalker.ffmq4.utils.async.AsyncTaskManager;

/**
 * <p>Holds global settings and managers shared among all clients in a given JVM.</p>
 */
public final class ClientEnvironment
{
    private static final Log log = LogFactory.getLog(ClientEnvironment.class);
    
    public static final String SETTINGS_FILE_NAME = "ffmq4.properties";
    
    // Global vars
   	private static NIOTcpMultiplexer multiplexer;
   	private static AsyncTaskManager asyncTaskManager;   	
    private static Settings settings;

    /**
     * Get the singleton instance
     */
    public static synchronized Settings getSettings()
    {
        if (settings == null)
            settings = loadSettings();
        return settings;
    }
    
    private static synchronized Settings loadSettings()
    {
        Properties settings = new Properties();
        try
        {
            // Load default settings first
            InputStream defaultIn = 
                FFMQClientSettings.class.getClassLoader().getResourceAsStream(
                    "net/timewalker/ffmq4/" + SETTINGS_FILE_NAME);
            if (defaultIn != null)
            {
                settings.load(defaultIn);
                defaultIn.close();
            }

            // Load settings in classpath
            InputStream userIn = FFMQClientSettings.class.getClassLoader().getResourceAsStream(
                    SETTINGS_FILE_NAME);
            if (userIn != null)
            {
                settings.load(userIn);
                userIn.close();
            }
        }
        catch (Exception e)
        {
            log.error("Cannot load settings", e);
        }
        return new Settings(settings);
    }

	/**
	 * Get the multiplexer singleton instance
	 * @return the multiplexer singleton instance
	 */
	public static synchronized NIOTcpMultiplexer getMultiplexer() throws PacketTransportException
	{
		if (multiplexer == null)
			multiplexer = new NIOTcpMultiplexer(getSettings(),true);
		return multiplexer;
	}
	
	/**
	 * Get the async. task manager singleton instance
	 * @return the manager instance
	 * @throws JMSException
	 */
	public static synchronized AsyncTaskManager getAsyncTaskManager() throws JMSException
	{
		if (asyncTaskManager == null)
		{
    		int threadPoolMinSize = getSettings().getIntProperty(FFMQCoreSettings.ASYNC_TASK_MANAGER_DELIVERY_THREAD_POOL_MINSIZE,0);
    		int threadPoolMaxIdle = getSettings().getIntProperty(FFMQCoreSettings.ASYNC_TASK_MANAGER_DELIVERY_THREAD_POOL_MAXIDLE,5);
    		int threadPoolMaxSize = getSettings().getIntProperty(FFMQCoreSettings.ASYNC_TASK_MANAGER_DELIVERY_THREAD_POOL_MAXSIZE,10);
			asyncTaskManager = new AsyncTaskManager("AsyncTaskManager-client-delivery",
													threadPoolMinSize,
													threadPoolMaxIdle,
													threadPoolMaxSize);
		}
		return asyncTaskManager;
	}
	
	/**
	 * Constructor (private)
	 */
	private ClientEnvironment()
	{
		super();
	}
}
