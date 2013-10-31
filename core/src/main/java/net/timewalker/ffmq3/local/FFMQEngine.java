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
package net.timewalker.ffmq3.local;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.TopicConnection;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.FFMQSecurityException;
import net.timewalker.ffmq3.local.connection.ClientIDRegistry;
import net.timewalker.ffmq3.local.connection.LocalConnection;
import net.timewalker.ffmq3.local.connection.LocalQueueConnection;
import net.timewalker.ffmq3.local.connection.LocalTopicConnection;
import net.timewalker.ffmq3.local.destination.LocalQueue;
import net.timewalker.ffmq3.local.destination.LocalTopic;
import net.timewalker.ffmq3.local.destination.subscription.DurableSubscriptionManager;
import net.timewalker.ffmq3.management.DestinationDefinitionProvider;
import net.timewalker.ffmq3.management.DestinationTemplateProvider;
import net.timewalker.ffmq3.management.FFMQEngineSetup;
import net.timewalker.ffmq3.management.TemplateMappingProvider;
import net.timewalker.ffmq3.management.destination.definition.QueueDefinition;
import net.timewalker.ffmq3.management.destination.definition.TopicDefinition;
import net.timewalker.ffmq3.management.destination.template.QueueTemplate;
import net.timewalker.ffmq3.management.destination.template.TopicTemplate;
import net.timewalker.ffmq3.security.SecurityConnectorProvider;
import net.timewalker.ffmq3.security.SecurityContext;
import net.timewalker.ffmq3.storage.data.DataStoreException;
import net.timewalker.ffmq3.storage.data.impl.BlockBasedDataStoreTools;
import net.timewalker.ffmq3.utils.ErrorTools;
import net.timewalker.ffmq3.utils.Settings;
import net.timewalker.ffmq3.utils.StringTools;
import net.timewalker.ffmq3.utils.async.AsyncTaskManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 *    Implementation of the core FFMQ engine.
 * </p>
 * <p>
 *    Typically created by an FFMQServer instance, but can also be created manually
 *    to be embedded directly in the using application JVM. 
 * </p>
 */
public final class FFMQEngine implements FFMQEngineMBean
{
    private static final Log log = LogFactory.getLog(FFMQEngine.class);
    
    private static Map<String,FFMQEngine> deployedEngines = new Hashtable<>();
    
    /**
     * Get a deployed engine instance by name
     */
    public static FFMQEngine getDeployedInstance( String name ) throws JMSException
    {
        FFMQEngine engine = deployedEngines.get(name);
        if (engine == null)
            throw new FFMQException("No deployed engine named "+name,"UNKNOWN_ENGINE");
        return engine;
    }
    
    //--------------------------------------------------------------------
    
    private String name;
    private FFMQEngineListener listener;
    private Map<String,LocalQueue> queueMap = new Hashtable<>();
    private Map<String,LocalTopic> topicMap = new Hashtable<>();
    private boolean deployed = false;
    private boolean securityEnabled;
    private FFMQEngineSetup setup;
    private DestinationDefinitionProvider destinationDefinitionProvider;
    private DestinationTemplateProvider destinationTemplateProvider;
    private TemplateMappingProvider templateMappingProvider;
    private DurableSubscriptionManager durableSubscriptionManager;
    
    // Thread pools
    private AsyncTaskManager notificationAsyncTaskManager;
    private AsyncTaskManager deliveryAsyncTaskManager;
    private AsyncTaskManager diskIOAsyncTaskManager;
    
    /**
     * Constructor
     * @throws FFMQException on configuration error
     */
    public FFMQEngine( String name , Settings engineSettings ) throws FFMQException
    {
        this(name,engineSettings,null);
    }
    
    /**
     * Constructor
     * @throws FFMQException on configuration error
     */
    public FFMQEngine( String name , Settings engineSettings , FFMQEngineListener listener ) throws FFMQException
    {
        this.name = name;
        this.listener = listener;
        this.setup = new FFMQEngineSetup(engineSettings);
        init();
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.LocalEngineMBean#getName()
     */
    @Override
	public String getName()
    {
        return name;
    }
    
    /**
     * Check that the engine is running
     */
    protected void checkDeployed() throws JMSException
    {
        if (!deployed)
            throw new FFMQException("Engine is stopped.","ENGINE_STOPPED");
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.LocalEngineMBean#isDeployed()
     */
    @Override
	public boolean isDeployed()
    {
        return deployed;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.FFMQEngineMBean#isSecurityEnabled()
     */
    @Override
	public boolean isSecurityEnabled()
    {
        return securityEnabled;
    }
    
    private void init()
    {
        this.destinationDefinitionProvider = new DestinationDefinitionProvider(setup);
        this.destinationTemplateProvider = new DestinationTemplateProvider(setup);
        this.templateMappingProvider = new TemplateMappingProvider(setup);
        this.securityEnabled = setup.isSecurityEnabled();
        this.durableSubscriptionManager = new DurableSubscriptionManager();
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.LocalEngineMBean#deploy()
     */
    public void deploy() throws JMSException
    {
        try
        {
            synchronized (deployedEngines)
            {
                if (deployed)
                    throw new FFMQException("Local engine is already deployed.","ENGINE_ALREADY_DEPLOYED");
                
                log.info("Deploying local engine '"+name+"'");
                this.destinationDefinitionProvider.loadExistingDefinitions();
                this.destinationTemplateProvider.loadExistingTemplates();
                this.templateMappingProvider.loadMappings();
                
                // AsyncTaskManager - Notification
               	this.notificationAsyncTaskManager = 
               		new AsyncTaskManager("AsyncTaskManager-notification-"+name,
               				             setup.getNotificationAsyncTaskManagerThreadPoolMinSize(),
               				             setup.getNotificationAsyncTaskManagerThreadPoolMaxIdle(),
               				             setup.getNotificationAsyncTaskManagerThreadPoolMaxSize());
                
                // AsyncTaskManager - Delivery
               	this.deliveryAsyncTaskManager = 
               		new AsyncTaskManager("AsyncTaskManager-delivery-"+name,
               				             setup.getDeliveryAsyncTaskManagerThreadPoolMinSize(),
               				             setup.getDeliveryAsyncTaskManagerThreadPoolMaxIdle(),
               				             setup.getDeliveryAsyncTaskManagerThreadPoolMaxSize());
               	
                // AsyncTaskManager - Disk I/O
               	this.diskIOAsyncTaskManager = 
               		new AsyncTaskManager("AsyncTaskManager-diskIO-"+name,
               				             setup.getDiskIOAsyncTaskManagerThreadPoolMinSize(),
               				             setup.getDiskIOAsyncTaskManagerThreadPoolMaxIdle(),
               				             setup.getDiskIOAsyncTaskManagerThreadPoolMaxSize());
    
                // Delete old temporary destinations
                deleteTemporaryDestinations();
    
                // Deploy existing destinations
                if (setup.doDeployQueuesOnStartup())
                    deployExistingQueues();
                if (setup.doDeployTopicsOnStartup())
                    deployExistingTopics();
                
                deployedEngines.put(name, this);
                deployed = true;
                log.info("Engine deployed (vm://"+name+")");
            }
            
            if (listener != null)
            	listener.engineDeployed();
        }
        catch (JMSException e)
        {
            log.error("Cannot deploy engine : "+e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get the destination template provider associated to this engine
     * @return the destination template provider associated to this engine
     */
    public DestinationTemplateProvider getDestinationTemplateProvider()
    {
        return destinationTemplateProvider;
    }
    
    /**
     * Get the template mapping provider associated to this engine
     * @return the template mapping provider associated to this engine
     */
    public TemplateMappingProvider getTemplateMappingProvider()
    {
        return templateMappingProvider;
    }
    
    private void deleteTemporaryDestinations() throws JMSException
    {
        String[] queueNames = destinationDefinitionProvider.getAllQueueNames();
        for (int i = 0 ; i < queueNames.length ; i++)
        {
            QueueDefinition queueDef = destinationDefinitionProvider.getQueueDefinition(queueNames[i]);
            if (queueDef.isTemporary())
            {
                log.info("Deleting old temporary queue : "+queueNames[i]);
                deleteQueue(queueNames[i],true);
            }
        }
        String[] topicNames = destinationDefinitionProvider.getAllTopicNames();
        for (int i = 0 ; i < topicNames.length ; i++)
        {
            TopicDefinition topicDef = destinationDefinitionProvider.getTopicDefinition(topicNames[i]);
            if (topicDef.isTemporary())
            {
                log.info("Deleting old temporary topic : "+topicNames[i]);
                deleteTopic(topicNames[i]);
            }
        }
    }
    
    private void deployExistingQueues()
    {
        log.info("Deploying existing queues");
        String[] queueNames = destinationDefinitionProvider.getAllQueueNames();
        for (int i = 0 ; i < queueNames.length ; i++)
        {
            try
            {
                getLocalQueue(queueNames[i]);
            }
            catch (JMSException e)
            {
            	ErrorTools.log(e, log);
            }
        }
    }
    
    private void deployExistingTopics()
    {
        log.info("Deploying existing topics");
        String[] topicNames = destinationDefinitionProvider.getAllTopicNames();
        for (int i = 0 ; i < topicNames.length ; i++)
        {
            try
            {
                getLocalTopic(topicNames[i]);
            }
            catch (JMSException e)
            {
            	ErrorTools.log(e, log);
            }
        }
    }
    
    /**
     * Get the engine setup
     */
    public FFMQEngineSetup getSetup()
    {
        return setup;
    }

    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.local.LocalEngineMBean#undeploy()
     */
    public void undeploy()
    {
        synchronized (deployedEngines)
        {
            if (!deployed)
                return;
            
            // Undeploy engine
            log.info("Undeploying local engine '"+name+"'");
            durableSubscriptionManager = null;
            
            
            // Stop async task manager - notification
            if (notificationAsyncTaskManager != null)
            {
            	notificationAsyncTaskManager.close();
            	notificationAsyncTaskManager = null;
            }
            
            // Stop async task manager - delivery
            if (deliveryAsyncTaskManager != null)
            {
            	deliveryAsyncTaskManager.close();
            	deliveryAsyncTaskManager = null;
            }

            // Undeploy queues
            synchronized (queueMap)
            {
            	List<LocalQueue> queues = new ArrayList<>();
            	queues.addAll(queueMap.values());
            	for (int i = 0; i < queues.size(); i++)
				{
                    LocalQueue localQueue = queues.get(i);
                    try
                    {
                    	undeployQueue(localQueue);
                    }
                    catch (JMSException e)
                    {
                    	ErrorTools.log(e, log);
                    }
                }
            }
            
            // Close topics
            synchronized (topicMap)
            {
            	List<LocalTopic> topics = new ArrayList<>();
            	topics.addAll(topicMap.values());
            	for (int i = 0; i < topics.size(); i++)
                {
                    LocalTopic localTopic = topics.get(i);
                    try
                    {
                    	undeployTopic(localTopic);
                    }
                    catch (JMSException e)
                    {
                    	ErrorTools.log(e, log);
                    }
                }
            }

            // Stop async task manager - disk I/O
            if (diskIOAsyncTaskManager != null)
            {
            	diskIOAsyncTaskManager.close();
            	diskIOAsyncTaskManager = null;
            }
            
            // Clear templates
            destinationTemplateProvider.clear();
            
            deployedEngines.remove(name);
            deployed = false;
        }
        
        if (listener != null)
        	listener.engineUndeployed();
    }
    
    /**
     * Open a new connection
     */
    public Connection openConnection( String userName , String password , String clientID ) throws JMSException
    {
        checkDeployed();
        if (clientID != null)
            ClientIDRegistry.getInstance().register(clientID);
        return new LocalConnection(this,
        						   getSecurityContext(userName, password),
                                   clientID);
    }
    
    /**
     * Open a new queue connection
     */
    public QueueConnection openQueueConnection( String userName , String password , String clientID ) throws JMSException
    {
        checkDeployed();
        if (clientID != null)
            ClientIDRegistry.getInstance().register(clientID);
        return new LocalQueueConnection(this,
        		                        getSecurityContext(userName, password),
                                        clientID);
    }
    
    /**
     * Open a new topic connection
     */
    public TopicConnection openTopicConnection( String userName , String password , String clientID ) throws JMSException
    {
        checkDeployed();
        if (clientID != null)
            ClientIDRegistry.getInstance().register(clientID);
        return new LocalTopicConnection(this,
        							    getSecurityContext(userName, password),
                                        clientID);
    }
    
    private SecurityContext getSecurityContext( String userName , String password ) throws JMSException
    {
        if (!securityEnabled)
            return null;
        
        if (userName == null || password == null)
            throw new FFMQSecurityException("Missing security credentials","MISSING_CREDENTIALS");
            
        return SecurityConnectorProvider.getConnector(setup).getContext(userName, password);
    }
    
    /**
     * Deploy a new temporary queue on this engine
     */
    public LocalQueue createTemporaryQueue( String queueName ) throws JMSException
    {
        String templateName = templateMappingProvider.getTemplateNameForQueue(queueName);
        if (StringTools.isEmpty(templateName))
            throw new FFMQException("No template matching queue : "+queueName,"MISSING_TEMPLATE_MAPPING");
        
        QueueTemplate queueTemplate = destinationTemplateProvider.getQueueTemplate(templateName);
        if (queueTemplate == null)
            throw new FFMQException("Queue template does not exist : "+templateName,"MISSING_TEMPLATE");
        
        QueueDefinition tempDef = queueTemplate.createQueueDefinition(queueName, true);
        return createQueue(tempDef);
    }

    /**
     * Deploy a new queue on this engine
     */
    public LocalQueue createQueue( QueueDefinition queueDef ) throws JMSException
    {
        queueDef.check();
        
        if (queueDef.hasPersistentStore() && setup.getDestinationDefinitionsDir() == null)
            throw new FFMQException("Cannot create a persistent queue if destinations folder is not set.","INVALID_CONFIGURATION");
        
        synchronized (queueMap)
        {
            if (destinationDefinitionProvider.getQueueDefinition(queueDef.getName()) != null)
                throw new FFMQException("Queue definition already exists : "+queueDef.getName(),"QUEUE_ALREADY_EXISTS");
 
            // Inject the new definition
            destinationDefinitionProvider.addQueueDefinition(queueDef);
            
            // If it's not a volatile queue, create the data files
            if (queueDef.hasPersistentStore())
            {
                log.debug("Creating local store for queue : "+queueDef.getName());
                try
                {
                    BlockBasedDataStoreTools.create(queueDef.getName(), 
                                                    queueDef.getDataFolder(), 
                                                    queueDef.getInitialBlockCount(), 
                                                    queueDef.getBlockSize(),
                                                    !queueDef.isTemporary());
                }
                catch (DataStoreException e)
                {
                    // Remove the queue definition
                    destinationDefinitionProvider.removeQueueDefinition(queueDef);
                    throw e;
                }
            }
                    
            return getLocalQueue(queueDef.getName());
        }
    }
    
    /**
     * Deploy a new temporary topic on this engine
     */
    public LocalTopic createTemporaryTopic( String topicName ) throws JMSException
    {
        String templateName = templateMappingProvider.getTemplateNameForTopic(topicName);
        if (StringTools.isEmpty(templateName))
            throw new FFMQException("No template matching topic : "+topicName,"MISSING_TEMPLATE_MAPPING");
        
        TopicTemplate topicTemplate = destinationTemplateProvider.getTopicTemplate(templateName);
        if (topicTemplate == null)
            throw new FFMQException("Topic template does not exist : "+templateName,"MISSING_TEMPLATE");

       TopicDefinition tempDef = topicTemplate.createTopicDefinition(topicName, true);
       return createTopic(tempDef);
    }
    
    /**
     * Create a new topic
     */
    public LocalTopic createTopic( TopicDefinition topicDef ) throws JMSException
    {
        topicDef.check();
        
        synchronized (topicMap)
        {
            if (destinationDefinitionProvider.getTopicDefinition(topicDef.getName()) != null)
                throw new FFMQException("Topic definition already exists : "+topicDef.getName(),"TOPIC_ALREADY_EXISTS");

            destinationDefinitionProvider.addTopicDefinition(topicDef);
            
            return getLocalTopic(topicDef.getName());
        }
    }
    
    /**
     * Undeploy a queue
     */
    public void deleteQueue( String queueName ) throws JMSException
    {
    	deleteQueue(queueName,false);
    }
    
    /**
     * Undeploy a queue
     */
    public void deleteQueue( String queueName , boolean force ) throws JMSException
    {
        synchronized (queueMap)
        {
            LocalQueue queue = queueMap.get(queueName);
            if (queue != null)
            {
                undeployQueue(queue);
                log.debug("Undeployed local queue : "+queueName);
            }
            
            QueueDefinition queueDef = destinationDefinitionProvider.getQueueDefinition(queueName);
            if (queueDef != null)
            {
                destinationDefinitionProvider.removeQueueDefinition(queueDef);
                
                if (queueDef.hasPersistentStore())
	                BlockBasedDataStoreTools.delete(queueDef.getName(), 
	                                                queueDef.getDataFolder(),
	                                                force);
            }
        }
    }
    
    /**
     * Undeploy a topic
     */
    public void deleteTopic( String topicName ) throws JMSException
    {
        synchronized (topicMap)
        {
            LocalTopic topic = topicMap.remove(topicName);
            if (topic != null)
            {
                undeployTopic(topic);
                log.debug("Undeployed local topic : "+topicName);
            }
                
            TopicDefinition topicDef = destinationDefinitionProvider.getTopicDefinition(topicName);
            if (topicDef != null)
                destinationDefinitionProvider.removeTopicDefinition(topicName);
        }
    }
    
    /**
     * Get a local queue by name
     */
    public LocalQueue getLocalQueue( String queueName ) throws JMSException
    {
        synchronized (queueMap)
        {
            LocalQueue queue = queueMap.get(queueName);
            if (queue == null)
                return loadOrAutoCreateQueue(queueName);
            
            return queue;
        }
    }
    
    /**
     * Test if a local queue exists by name
     */
    public boolean localQueueExists( String queueName ) throws JMSException
    {
        synchronized (queueMap)
        {
            LocalQueue queue = queueMap.get(queueName);
            if (queue != null)
                return true;
                
            // Check if a definition exists
            if (destinationDefinitionProvider.getQueueDefinition(queueName) != null)
                return true;
            
            return false;
        }
    }
    
    private void deployQueue( LocalQueue queue )
    {
        queueMap.put(queue.getName(),queue);
        if (listener != null)
        	listener.queueDeployed(queue);
    }
    
    private void deployTopic( LocalTopic topic )
    {
        topicMap.put(topic.getName(),topic);
        if (listener != null)
        	listener.topicDeployed(topic);
    }
    
    private void undeployQueue( LocalQueue queue ) throws JMSException
    {
        queue.close();
        queueMap.remove(queue.getName());
        
        // Destroy temporary queues automatically
        if (queue.getDefinition().isTemporary())
            destinationDefinitionProvider.removeQueueDefinition(queue.getDefinition());
        
        if (listener != null)
        	listener.queueUndeployed(queue);
    }
    
    private void undeployTopic( LocalTopic topic ) throws JMSException
    {
        topic.close();
        topicMap.remove(topic.getName());
        
        if (listener != null)
        	listener.topicUndeployed(topic);
    }
    
    private LocalQueue loadOrAutoCreateQueue( String queueName ) throws JMSException
    {
        QueueDefinition queueDef = destinationDefinitionProvider.getQueueDefinition(queueName);
        if (queueDef != null)
        {
            LocalQueue queue = new LocalQueue(this,queueDef);
            deployQueue(queue);
            return queue;
        }
        
        // Queue auto-creation
        if (setup.doAutoCreateQueues())
        {
            // Look for matching template
            String templateName = templateMappingProvider.getTemplateNameForQueue(queueName);
            if (templateName != null)
            {
                QueueTemplate queueTemplate = destinationTemplateProvider.getQueueTemplate(templateName); 
                if (queueTemplate != null)
                    return createQueue(queueTemplate.createQueueDefinition(queueName, false));
            }
        }
        
        throw new FFMQException("Queue does not exist : "+queueName,"QUEUE_DOES_NOT_EXIST");
    }
    
    /**
     * Get a local topic by name
     */
    public LocalTopic getLocalTopic( String topicName ) throws JMSException
    {
        synchronized (topicMap)
        {
            LocalTopic topic = topicMap.get(topicName);
            if (topic == null)
                return loadOrAutoCreateTopic(topicName);
            
            return topic;
        }
    }
    
    /**
     * Test if a local topic exists by name
     */
    public boolean localTopicExists( String topicName ) throws JMSException
    {
        synchronized (topicMap)
        {
            LocalTopic topic = topicMap.get(topicName);
            if (topic != null)
                return true;
            
            // Check if a definition exists
            if (destinationDefinitionProvider.getTopicDefinition(topicName) != null)
                return true;
            
            return false;
        }
    }
    
    private LocalTopic loadOrAutoCreateTopic( String topicName ) throws JMSException
    {
        TopicDefinition topicDef = destinationDefinitionProvider.getTopicDefinition(topicName);
        if (topicDef != null)
        {
            LocalTopic topic = new LocalTopic(topicDef);
            deployTopic(topic);
            return topic;
        }
        
        // Topic auto-creation
        if (setup.doAutoCreateTopics())
        {
            String templateName = templateMappingProvider.getTemplateNameForTopic(topicName);
            if (templateName != null)
            {
                TopicTemplate topicTemplate = destinationTemplateProvider.getTopicTemplate(templateName); 
                if (topicTemplate != null)
                    return createTopic(topicTemplate.createTopicDefinition(topicName, false));
            }
        }
        
        throw new FFMQException("Topic does not exist : "+topicName,"TOPIC_DOES_NOT_EXIST");
    }
    
    public void subscribe( String clientID , String subscriptionName ) throws JMSException
    {
        if (durableSubscriptionManager == null)
            throw new FFMQException("Engine is stopped.","ENGINE_STOPPED");
        
    	if (durableSubscriptionManager.register(clientID, subscriptionName))
    		log.debug("Storing a new durable subscription : "+clientID+"-"+subscriptionName);
    	else
    		log.debug("Subscription already exist : "+clientID+"-"+subscriptionName);
    }
    
    /**
     * Unsubscribe a durable subscriber from all related topics
     */
    public void unsubscribe( String clientID , String subscriptionName ) throws JMSException
    {
        if (durableSubscriptionManager == null)
            throw new FFMQException("Engine is stopped.","ENGINE_STOPPED");
        
    	// Check that the registration is valid
    	if (!durableSubscriptionManager.isRegistered(clientID, subscriptionName))
    		throw new InvalidDestinationException("Invalid subscription : "+subscriptionName+" for client "+clientID); // [JMS spec]
    	
    	// Try to remove all remanent subscriptions first
        synchronized (topicMap)
        {
            Iterator<LocalTopic> topics = topicMap.values().iterator();
            while (topics.hasNext())
            {
                LocalTopic topic = topics.next();
                topic.unsubscribe(clientID,subscriptionName);
            }
        }
        
        // Then delete subscription related queues
        String subscriberID = clientID+"-"+subscriptionName;
    	synchronized (queueMap)
		{
    		List<String> queuesToDelete = new ArrayList<>();
    		Iterator<String> queueNames = queueMap.keySet().iterator();
    		while (queueNames.hasNext())
    		{
    			String queueName = queueNames.next();
    			if (queueName.endsWith(subscriberID))
    				queuesToDelete.add(queueName);
    		}
    		
    		// Delete matching queues
    		for (int i = 0; i < queuesToDelete.size(); i++)
    			deleteQueue(queuesToDelete.get(i));
		}
    	
    	// Clean-up the subscription itself
    	if (!durableSubscriptionManager.unregister(clientID, subscriptionName))
    		log.error("Unknown durable subscription : "+clientID+"-"+subscriptionName);
    }
    
    /**
     * Get the engine async. notification task manager
	 * @return the engine async. notification task manager
	 */
	public AsyncTaskManager getNotificationAsyncTaskManager() throws JMSException
	{
		if (notificationAsyncTaskManager == null)
            throw new FFMQException("Engine is stopped.","ENGINE_STOPPED");
		return notificationAsyncTaskManager;
	}
    
    /**
     * Get the engine async. delivery task manager
	 * @return the engine async. delivery task manager
	 */
	public AsyncTaskManager getDeliveryAsyncTaskManager() throws JMSException
	{
		if (deliveryAsyncTaskManager == null)
            throw new FFMQException("Engine is stopped.","ENGINE_STOPPED");
		return deliveryAsyncTaskManager;
	}
	
	/**
     * Get the engine async. disk I/O task manager
	 * @return the engine async. disk I/O task manager
	 */
	public AsyncTaskManager getDiskIOAsyncTaskManager() throws JMSException
	{
		if (diskIOAsyncTaskManager == null)
            throw new FFMQException("Engine is stopped.","ENGINE_STOPPED");
		return diskIOAsyncTaskManager;
	}
	
	/**
	 * @return the destinationDefinitionProvider
	 */
	public DestinationDefinitionProvider getDestinationDefinitionProvider()
	{
		return destinationDefinitionProvider;
	}
	
	/* (non-Javadoc)
	 * @see net.timewalker.ffmq3.local.FFMQEngineMBean#clearAllStatistics()
	 */
	@Override
	public void resetAllStatistics()
	{
		synchronized (queueMap)
		{
			Iterator<LocalQueue> queues = queueMap.values().iterator();
			while (queues.hasNext())
			{
				LocalQueue queue = queues.next();
				queue.resetStats();
			}
		}
		synchronized (topicMap)
		{
			Iterator<LocalTopic> topics = topicMap.values().iterator();
			while (topics.hasNext())
			{
				LocalTopic topic = topics.next();
				topic.resetStats();
			}
		}
	}
}
