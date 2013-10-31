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

import java.io.File;

import net.timewalker.ffmq3.FFMQCoreSettings;
import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.utils.Settings;
import net.timewalker.ffmq3.utils.StringTools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Extract engine configuration settings from the given {@link Settings} object.</p>
 */
public final class FFMQEngineSetup
{
    private static final Log log = LogFactory.getLog(FFMQEngineSetup.class);
    
	private Settings settings;
	
    // Cache
    private boolean securityEnabled;
    private File destinationDefinitionsDir;
    private File bridgeDefinitionsDir;
    private File templatesDir;
    private File templateMappingFile;
    private File defaultDataDir;
    private boolean autoCreateQueues;
    private boolean autoCreateTopics;
    private boolean deployQueuesOnStartup;
    private boolean deployTopicsOnStartup;
    private int consumerPrefetchSize;
    private int notificationAsyncTaskManagerThreadPoolMinSize;
	private int notificationAsyncTaskManagerThreadPoolMaxIdle;
	private int notificationAsyncTaskManagerThreadPoolMaxSize;
	private int deliveryAsyncTaskManagerThreadPoolMinSize;
	private int deliveryAsyncTaskManagerThreadPoolMaxIdle;
	private int deliveryAsyncTaskManagerThreadPoolMaxSize;
	private int diskIOAsyncTaskManagerThreadPoolMinSize;
	private int diskIOAsyncTaskManagerThreadPoolMaxIdle;
	private int diskIOAsyncTaskManagerThreadPoolMaxSize;
	private int watchdogConsumerInactivityTimeout;
	private String securityConnectorType;
	private long redeliveryDelay;
	private int internalNotificationQueueMaxSize;
	
    /**
     * Constructor
     */
    public FFMQEngineSetup( Settings settings ) throws FFMQException
    {
    	this.settings = settings;
        setup(settings);
    }

    /**
	 * @return the settings
	 */
	public Settings getSettings()
	{
		return settings;
	}
    
    private void setup( Settings settings ) throws FFMQException
    {
        // Security
        securityEnabled = settings.getBooleanProperty(FFMQCoreSettings.SECURITY_ENABLED,false);
        securityConnectorType = settings.getStringProperty(FFMQCoreSettings.SECURITY_CONNECTOR,"undefined");
        
        // Destination definitions directory
        String destinationDefinitionDirPath = settings.getStringProperty(FFMQCoreSettings.DESTINATION_DEFINITIONS_DIR, null);
        if (destinationDefinitionDirPath != null)
        {
            destinationDefinitionsDir = new File(destinationDefinitionDirPath);
            if (!destinationDefinitionsDir.isDirectory())
                throw new FFMQException("Destination definitions directory does not exist : "+destinationDefinitionsDir.getAbsolutePath(),"FS_ERROR");
        }
        else
            log.warn("Destination definitions directory is not set, running in memory only mode.");
        
        // Bridge definitions directory
        String bridgeDefinitionDirPath = settings.getStringProperty(FFMQCoreSettings.BRIDGE_DEFINITIONS_DIR, null);
        if (bridgeDefinitionDirPath != null)
        {
            bridgeDefinitionsDir = new File(bridgeDefinitionDirPath);
            if (!bridgeDefinitionsDir.isDirectory())
                throw new FFMQException("Bridge definitions directory does not exist : "+bridgeDefinitionsDir.getAbsolutePath(),"FS_ERROR");
        }
        
        // Templates directory
        String templatesDirPath = settings.getStringProperty(FFMQCoreSettings.TEMPLATES_DIR, null);
        if (templatesDirPath == null)
            throw new FFMQException("Templates directory not defined : "+FFMQCoreSettings.TEMPLATES_DIR,"MISSING_SETTING");
        templatesDir = new File(templatesDirPath);
        if (!templatesDir.isDirectory())
            throw new FFMQException("Templates directory does not exist : "+templatesDir.getAbsolutePath(),"FS_ERROR");
        
        // Templates mapping file
        String templatesMappingPath = settings.getStringProperty(FFMQCoreSettings.TEMPLATE_MAPPING_FILE, null);
        if (!StringTools.isEmpty(templatesMappingPath))
        {
            templateMappingFile = new File(templatesMappingPath);
            if (!templateMappingFile.canRead())
                throw new FFMQException("Template mapping file does not exist : "+templateMappingFile.getAbsolutePath(),"FS_ERROR");
        }

        // Default data directory
        String defaultDataDirPath = settings.getStringProperty(FFMQCoreSettings.DEFAULT_DATA_DIR, null);
        if (defaultDataDirPath == null)
            throw new FFMQException("Default data directory not defined : "+FFMQCoreSettings.DEFAULT_DATA_DIR,"MISSING_SETTING");
        defaultDataDir = new File(defaultDataDirPath);
        if (!defaultDataDir.isDirectory())
            throw new FFMQException("Default data directory does not exist : "+defaultDataDir.getAbsolutePath(),"FS_ERROR");
        
        // Auto-create
        autoCreateQueues = settings.getBooleanProperty(FFMQCoreSettings.AUTO_CREATE_QUEUES, false);
        autoCreateTopics = settings.getBooleanProperty(FFMQCoreSettings.AUTO_CREATE_TOPICS, false);
        
        // Deploy on startup
        deployQueuesOnStartup = settings.getBooleanProperty(FFMQCoreSettings.DEPLOY_QUEUES_ON_STARTUP, false);
        deployTopicsOnStartup = settings.getBooleanProperty(FFMQCoreSettings.DEPLOY_TOPICS_ON_STARTUP, false);
        
        // Async Task Manager - Notification
		this.notificationAsyncTaskManagerThreadPoolMinSize = settings.getIntProperty(FFMQCoreSettings.ASYNC_TASK_MANAGER_NOTIFICATION_THREAD_POOL_MINSIZE, 5);
		this.notificationAsyncTaskManagerThreadPoolMaxIdle = settings.getIntProperty(FFMQCoreSettings.ASYNC_TASK_MANAGER_NOTIFICATION_THREAD_POOL_MAXIDLE, 10);
		this.notificationAsyncTaskManagerThreadPoolMaxSize = settings.getIntProperty(FFMQCoreSettings.ASYNC_TASK_MANAGER_NOTIFICATION_THREAD_POOL_MAXSIZE, 15);
        
        // Async Task Manager - Delivery
		this.deliveryAsyncTaskManagerThreadPoolMinSize = settings.getIntProperty(FFMQCoreSettings.ASYNC_TASK_MANAGER_DELIVERY_THREAD_POOL_MINSIZE, 5);
		this.deliveryAsyncTaskManagerThreadPoolMaxIdle = settings.getIntProperty(FFMQCoreSettings.ASYNC_TASK_MANAGER_DELIVERY_THREAD_POOL_MAXIDLE, 5);
		this.deliveryAsyncTaskManagerThreadPoolMaxSize = settings.getIntProperty(FFMQCoreSettings.ASYNC_TASK_MANAGER_DELIVERY_THREAD_POOL_MAXSIZE, 10);
		
		// Async Task Manager - Disk I/O
		this.diskIOAsyncTaskManagerThreadPoolMinSize = settings.getIntProperty(FFMQCoreSettings.ASYNC_TASK_MANAGER_DISKIO_THREAD_POOL_MINSIZE, 2);
		this.diskIOAsyncTaskManagerThreadPoolMaxIdle = settings.getIntProperty(FFMQCoreSettings.ASYNC_TASK_MANAGER_DISKIO_THREAD_POOL_MAXIDLE, 2);
		this.diskIOAsyncTaskManagerThreadPoolMaxSize = settings.getIntProperty(FFMQCoreSettings.ASYNC_TASK_MANAGER_DISKIO_THREAD_POOL_MAXSIZE, 4);
        
        // Prefetching
        consumerPrefetchSize = settings.getIntProperty(FFMQCoreSettings.CONSUMER_PREFETCH_SIZE,10);
        if (consumerPrefetchSize < 1)
        	consumerPrefetchSize = 1;
        
        // Watchdog
        watchdogConsumerInactivityTimeout = settings.getIntProperty(FFMQCoreSettings.WATCHDOG_CONSUMER_INACTIVITY_TIMEOUT, 5);
        
        // Delivery
        redeliveryDelay = settings.getLongProperty(FFMQCoreSettings.DELIVERY_REDELIVERY_DELAY,0);
        
        // Notification queue
        internalNotificationQueueMaxSize = settings.getIntProperty(FFMQCoreSettings.NOTIFICATION_QUEUE_MAX_SIZE,200);
    }
    
    /**
	 * @return the internalNotificationQueueMaxSize
	 */
	public int getInternalNotificationQueueMaxSize()
	{
		return internalNotificationQueueMaxSize;
	}
    
    public boolean isSecurityEnabled()
    {
        return securityEnabled;
    }

    public File getDestinationDefinitionsDir()
    {
        return destinationDefinitionsDir;
    }
    
    /**
	 * @return the bridgeDefinitionsDir
	 */
	public File getBridgeDefinitionsDir()
	{
		return bridgeDefinitionsDir;
	}
    
    public File getTemplatesDir()
    {
        return templatesDir;
    }
    
    /**
     * @return the templateMappingFile
     */
    public File getTemplateMappingFile()
    {
        return templateMappingFile;
    }

    /**
     * @return the defaultDataDir
     */
    public File getDefaultDataDir()
    {
        return defaultDataDir;
    }

    /**
     * @return the autoCreateQueues
     */
    public boolean doAutoCreateQueues()
    {
        return autoCreateQueues;
    }

    /**
     * @return the autoCreateTopics
     */
    public boolean doAutoCreateTopics()
    {
        return autoCreateTopics;
    }

    /**
     * @return the deployQueuesOnStartup
     */
    public boolean doDeployQueuesOnStartup()
    {
        return deployQueuesOnStartup;
    }
    
    /**
     * @return the deployQueuesOnStartup
     */
    public boolean doDeployTopicsOnStartup()
    {
        return deployTopicsOnStartup;
    }
    
    /**
	 * @return the consumerPrefetchSize
	 */
	public int getConsumerPrefetchSize()
	{
		return consumerPrefetchSize;
	}

	/**
	 * @return the notificationAsyncTaskManagerThreadPoolMinSize
	 */
	public int getNotificationAsyncTaskManagerThreadPoolMinSize()
	{
		return notificationAsyncTaskManagerThreadPoolMinSize;
	}
	
	/**
	 * @return the notificationAsyncTaskManagerThreadPoolMaxIdle
	 */
	public int getNotificationAsyncTaskManagerThreadPoolMaxIdle()
	{
		return notificationAsyncTaskManagerThreadPoolMaxIdle;
	}
	
	/**
	 * @return the notificationAsyncTaskManagerThreadPoolMaxSize
	 */
	public int getNotificationAsyncTaskManagerThreadPoolMaxSize()
	{
		return notificationAsyncTaskManagerThreadPoolMaxSize;
	}
	
	/**
	 * @return the deliveryAsyncTaskManagerThreadPoolMinSize
	 */
	public int getDeliveryAsyncTaskManagerThreadPoolMinSize()
	{
		return deliveryAsyncTaskManagerThreadPoolMinSize;
	}

	/**
	 * @return the deliveryAsyncTaskManagerThreadPoolMaxIdle
	 */
	public int getDeliveryAsyncTaskManagerThreadPoolMaxIdle()
	{
		return deliveryAsyncTaskManagerThreadPoolMaxIdle;
	}

	/**
	 * @return the deliveryAsyncTaskManagerThreadPoolMaxSize
	 */
	public int getDeliveryAsyncTaskManagerThreadPoolMaxSize()
	{
		return deliveryAsyncTaskManagerThreadPoolMaxSize;
	}

	/**
	 * @return the diskIOAsyncTaskManagerThreadPoolMinSize
	 */
	public int getDiskIOAsyncTaskManagerThreadPoolMinSize()
	{
		return diskIOAsyncTaskManagerThreadPoolMinSize;
	}

	/**
	 * @return the diskIOAsyncTaskManagerThreadPoolMaxIdle
	 */
	public int getDiskIOAsyncTaskManagerThreadPoolMaxIdle()
	{
		return diskIOAsyncTaskManagerThreadPoolMaxIdle;
	}

	/**
	 * @return the diskIOAsyncTaskManagerThreadPoolMaxSize
	 */
	public int getDiskIOAsyncTaskManagerThreadPoolMaxSize()
	{
		return diskIOAsyncTaskManagerThreadPoolMaxSize;
	}

	/**
	 * @return the watchdogConsumerInactivityTimeout
	 */
	public int getWatchdogConsumerInactivityTimeout()
	{
		return watchdogConsumerInactivityTimeout;
	}
	
	/**
	 * @return the securityConnectorType
	 */
	public String getSecurityConnectorType()
	{
		return securityConnectorType;
	}
	
	/**
	 * @return the redeliveryDelay
	 */
	public long getRedeliveryDelay()
	{
		return redeliveryDelay;
	}
}
