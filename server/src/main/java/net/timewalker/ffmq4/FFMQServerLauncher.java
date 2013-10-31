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
package net.timewalker.ffmq4;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import net.timewalker.ffmq4.logging.Log4JTools;
import net.timewalker.ffmq4.utils.Settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Launcher for an FFMQ Server instance.</p>
 */
public final class FFMQServerLauncher
{
	protected static final Log log = LogFactory.getLog(FFMQServerLauncher.class);
	
    private static final String DEFAULT_SERVER_CONF_FILE = "conf/ffmq-server.properties";
    
    private static final int SHUTDOWN_TIMEOUT = 60; // seconds
    
    /**
     * Main
     */
    public static void main(String[] args)
    {
        try
        {
        	setupSystemProperties();
            Settings settings = parseCommandLine(args);
        
	        // Create a server instance
	        FFMQServer server = new FFMQServer("engine1",settings);
	        Runtime.getRuntime().addShutdownHook(new ShutdownHook(server));
	            
	        // Start server
	        server.run();
	        
	        System.exit(0);
        }
        catch (IllegalArgumentException e)
        {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.exit(-2);
        }
    }
    
    private static void setupSystemProperties()
    {
    	// Application home
    	String ffmqHome = System.getProperty("FFMQ_HOME");
    	if (ffmqHome == null)
    	{
    		ffmqHome = "..";
    		System.setProperty("FFMQ_HOME",ffmqHome);
    	}
    	
    	// Application base
    	String ffmqBase = System.getProperty("FFMQ_BASE");
    	if (ffmqBase == null)
    		System.setProperty("FFMQ_BASE",ffmqHome);
    }
    
    private static Settings parseCommandLine(String[] args)
    {
        String confFilePath = null;
        for (int i = 0 ; i < args.length ; i++)
        {
            if (args[i].equals("-conf"))
            {
                i++;
                if (i == args.length)
                    throw new IllegalArgumentException("Missing value after parameter -conf");
                confFilePath = args[i];
            }
            else
            	throw new IllegalArgumentException("Unknown command-line argument : "+args[i]);
        }
        
        // Load configuration file
        File confFile;
        if (confFilePath != null)
        	confFile = new File(confFilePath);
        else
        	confFile = new File(System.getProperty("FFMQ_BASE"),DEFAULT_SERVER_CONF_FILE);
        if (!confFile.canRead())
            throw new IllegalArgumentException("Cannot access config file : "+confFile.getAbsolutePath());
        
        Properties props = loadServerProperties(confFile);
        Log4JTools.initializeLog4J(props);
        
        return new Settings(props);
    }
    
    private static Properties loadServerProperties( File configFile )
    {
        try
        {
            Properties settings = new Properties();
            FileInputStream in = new FileInputStream(configFile);
            settings.load(in);
            in.close();
            
            return settings;
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Cannot load server settings : "+e);
        }
    }
    
    //---------------------------------------------------------------------------------------
    
    private static class ShutdownHook extends Thread
    {
    	private FFMQServer server;
    	
    	/**
		 * Constructor
		 */
		public ShutdownHook( FFMQServer server )
		{
			super();
			this.server = server;
		}
    	
    	/* (non-Javadoc)
    	 * @see java.lang.Thread#run()
    	 */
    	@Override
		public void run()
    	{
    		try
    		{
	    		if (server.isStarted())
	    		{
	    			log.info("Caught signal, asking the server to shutdown");
	    			server.pleaseStop();
	    			
	    			// Wait for the server to stop
	    			long startTime = System.currentTimeMillis();
	    			while (server.isStarted())
	    			{
	    				Thread.sleep(100);
	    				
	    				long now = System.currentTimeMillis();
	    				if (now-startTime > SHUTDOWN_TIMEOUT*1000)
	    				{
	    					log.fatal("Timeout waiting for server shutdown ("+SHUTDOWN_TIMEOUT+"s)");
	    					return;
	    				}
	    			}
	    		}
    		}
    		catch (Throwable e)
    		{
    			log.fatal("Cannot shutdown server",e);
    		}
    	}
    }  
}
