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
package net.timewalker.ffmq3.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.JMSException;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.FFMQServer;
import net.timewalker.ffmq3.utils.Settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * FFMQEngineBean
 */
public class FFMQServerBean
{
	private static final Log log = LogFactory.getLog(FFMQServerBean.class);
	
	// Settings
	private String engineName;
	private String configLocation;
	
	// Runtime
	private FFMQServer server;
	
	/**
	 * Constructor
	 */
	public FFMQServerBean()
	{
		super();
	}

	/**
	 * @return the engineName
	 */
	public String getEngineName()
	{
		return engineName;
	}

	/**
	 * @param engineName the engineName to set
	 */
	public void setEngineName(String engineName)
	{
		this.engineName = engineName;
	}

	/**
	 * @return the configLocation
	 */
	public String getConfigLocation()
	{
		return configLocation;
	}

	/**
	 * @param configLocation the configLocation to set
	 */
	public void setConfigLocation(String configLocation)
	{
		this.configLocation = configLocation;
	}
	
	private void checkProperties() throws JMSException
	{
		if (engineName == null || engineName.length() == 0)
			throw new FFMQException("Bean property 'engineName' is required","INVALID_BEAN_CONFIG");
		if (configLocation == null)
			throw new FFMQException("Bean property 'configLocation' is required","INVALID_BEAN_CONFIG");
	}
	
	private Settings loadConfig() throws JMSException
	{
		try
        {
			Properties settings = new Properties();
			
			if (configLocation.startsWith("classpath:"))
			{
				String resourcePath = configLocation.substring(10);
				InputStream in = FFMQServerBean.class.getClassLoader().getResourceAsStream(resourcePath);
				if (in == null)
					throw new IllegalArgumentException("Cannot find configuration resource in classpath : "+resourcePath);
	            settings.load(in);
	            in.close();
			}
			else
			{
				File configFile = new File(configLocation);
				if (!configFile.canRead())
					throw new IllegalArgumentException("Cannot read configuration file : "+configFile.getAbsolutePath());
				InputStream in = new FileInputStream(configFile);
	            settings.load(in);
	            in.close();
			}
            
            return new Settings(settings);
        }
        catch (Exception e)
        {
            throw new FFMQException("Cannot load engine settings","INVALID_BEAN_CONFIG",e);
        }
	}
	
	/**
	 * Starts the server bean
	 * @throws JMSException on startup error
	 */
	public void start() throws JMSException
	{
		if (server == null)
		{
			checkProperties();
			log.info("Starting FFMQServerBean ...");
			Settings settings = loadConfig();
			server = new FFMQServer(engineName,settings);
			server.start();
		}
	}
	
	/**
	 * Stops the server bean
	 */
	public void stop()
	{
		if (server != null)
		{
			server.shutdown();
			server = null;
		}
	}
}
