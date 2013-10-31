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
import java.util.List;
import java.util.Vector;

import javax.jms.JMSException;
import javax.management.ObjectName;

import net.timewalker.ffmq4.admin.RemoteAdministrationThread;
import net.timewalker.ffmq4.cluster.bridge.JMSBridge;
import net.timewalker.ffmq4.jmx.JMXAgent;
import net.timewalker.ffmq4.jmx.rmi.JMXOverRMIAgent;
import net.timewalker.ffmq4.listeners.ClientListener;
import net.timewalker.ffmq4.listeners.tcp.io.TcpListener;
import net.timewalker.ffmq4.listeners.tcp.nio.NIOTcpListener;
import net.timewalker.ffmq4.local.FFMQEngine;
import net.timewalker.ffmq4.local.FFMQEngineListener;
import net.timewalker.ffmq4.local.destination.LocalQueue;
import net.timewalker.ffmq4.local.destination.LocalTopic;
import net.timewalker.ffmq4.management.BridgeDefinitionProvider;
import net.timewalker.ffmq4.management.bridge.BridgeDefinition;
import net.timewalker.ffmq4.utils.InetUtils;
import net.timewalker.ffmq4.utils.Settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Implementation of an FFMQ Server instance.</p>
 */
public final class FFMQServer implements FFMQServerMBean, FFMQEngineListener, Runnable
{
    private static final Log log = LogFactory.getLog(FFMQServer.class);
    
    private static final int ADMIN_THREAD_STOP_TIMEOUT = 30; // seconds
    
    // Settings
    private String engineName;
    private Settings settings;

    // Runtime
    private long startupTime;
    private JMXAgent jmxAgent;
    private FFMQEngine engine;
    private ClientListener tcpListener;
    private RemoteAdministrationThread adminThread;
    private BridgeDefinitionProvider bridgeDefinitionProvider;
    private List<JMSBridge> bridges = new Vector<>();
    private boolean started;
    private boolean stopRequired;
    private boolean inRunnableMode;
     
    /**
     * Constructor
     */
    public FFMQServer( String engineName , Settings settings ) throws JMSException
    {
    	this.engineName = engineName;
        this.settings = settings;
        init();
    }
    
    private void init() throws JMSException
    {
        // Bridge definitions directory
        String bridgeDefinitionDirPath = settings.getStringProperty(FFMQCoreSettings.BRIDGE_DEFINITIONS_DIR, null);
        if (bridgeDefinitionDirPath != null)
        {
            File bridgeDefinitionsDir = new File(bridgeDefinitionDirPath);
            if (!bridgeDefinitionsDir.isDirectory())
                throw new FFMQException("Bridge definitions directory does not exist : "+bridgeDefinitionsDir.getAbsolutePath(),"FS_ERROR");
            this.bridgeDefinitionProvider = new BridgeDefinitionProvider(bridgeDefinitionsDir);    
        }
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.FFMQServerMBean#getVersion()
     */
    @Override
	public String getVersion()
    {
    	return FFMQVersion.getProviderMajorVersion()+"."+FFMQVersion.getProviderMinorVersion()+"."+FFMQVersion.getProviderReleaseVersion();
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.FFMQServerMBean#start()
     */
    @Override
	public synchronized boolean start()
    {
    	if (started)
    		return false;
    	
    	try
    	{
	    	long startTime = System.currentTimeMillis();
	    	
	    	// Banners
	        log.info("FFMQ server "+getVersion()+
	                 " (Java "+System.getProperty("java.version")+")");
	        log.info("Running on "+System.getProperty("os.name","?")+" "+System.getProperty("os.version","?"));
	        log.info("JVM is "+System.getProperty("java.vm.vendor","?")+" "+System.getProperty("java.runtime.version","?")+" ["+System.getProperty("java.vm.name","?")+"] at "+System.getProperty("java.home","?"));
	        log.info("Server local engine name is '"+engineName+"'");
	    	
	        log.debug("Server startup ...");
	        
	        // Deploy JMX support
	        boolean jmxAgentEnabled = settings.getBooleanProperty(FFMQCoreSettings.JMX_AGENT_ENABLED, false);
	        int jmxJndiRmiPort = settings.getIntProperty(FFMQCoreSettings.JMX_AGENT_JNDI_RMI_PORT,10003);
	        String jmxRmiListenAddr = InetUtils.resolveAutoInterfaceAddress(settings.getStringProperty(FFMQCoreSettings.JMX_AGENT_RMI_LISTEN_ADDR, "0.0.0.0"));
	        if (jmxAgentEnabled)
	        {
	        	jmxAgent = new JMXOverRMIAgent("FFMQ-server",jmxJndiRmiPort,jmxRmiListenAddr);
	        	try
	        	{
	        		jmxAgent.register(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Server"), this);
	        	}
	        	catch (Exception e)
	        	{
	        		log.error("Cannot register server on JMX agent",e);
	        	}
	        }
	        
	        // Deploy local engine
	        engine = new FFMQEngine(engineName,settings,this);
	        try
	    	{
	    		if (jmxAgent != null)
	    			jmxAgent.register(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engine.getName()), engine);
	    	}
	    	catch (Exception e)
	    	{
	    		log.error("Cannot register local engine on JMX agent",e);
	    	}
	        engine.deploy();
	        
	        // Deploy listeners
	        String listenAddr          = InetUtils.resolveAutoInterfaceAddress(settings.getStringProperty(FFMQServerSettings.LISTENER_TCP_LISTEN_ADDR,null));
	        int listenPort             = settings.getIntProperty(FFMQServerSettings.LISTENER_TCP_LISTEN_PORT,FFMQConstants.DEFAULT_SERVER_PORT);
	        boolean tcpListenerEnabled = settings.getBooleanProperty(FFMQServerSettings.LISTENER_TCP_ENABLED,true);
	        boolean useNIOListener     = settings.getBooleanProperty(FFMQServerSettings.LISTENER_TCP_USE_NIO,false);
	        boolean useSSL             = settings.getBooleanProperty(FFMQCoreSettings.TRANSPORT_TCP_SSL_ENABLED, false);
	        if (useSSL && useNIOListener)
	        	throw new FFMQException("Cannot enable SSL & NIO listener at the same time.","CONFIGURATION_ERROR"); // JDK 1.4 limitation
	
	        if (tcpListenerEnabled)
	        {
	        	if (useNIOListener)
	        		tcpListener = new NIOTcpListener(engine,listenAddr,listenPort,settings,jmxAgent);
	        	else
	        		tcpListener = new TcpListener(engine,listenAddr,listenPort,settings,jmxAgent);
	
	        	if (jmxAgent != null)
	        	{
					try
					{
						jmxAgent.register(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Listeners,listener="+tcpListener.getName()),tcpListener);
					}
					catch (Exception e)
					{
						log.error("Cannot register listener on JMX agent",e);
					}
	        	}
	        		
	            tcpListener.start();
	        }
	        
	        // Deploy remote admin support
	        boolean remoteAdminEnabled = settings.getBooleanProperty(FFMQServerSettings.REMOTE_ADMIN_ENABLED,false);
	        if (remoteAdminEnabled)
	        {
	            adminThread = new RemoteAdministrationThread(this,engine);
	            adminThread.start();
	            try
	            {
	                adminThread.waitForStartup();
	            }
	            catch (InterruptedException e)
	            {
	                throw new FFMQException("Deploy was interrupted while waiting for the admin thread to start","INTERNAL_ERROR");
	            }
	        }
	        
	        // Deploy bridges
	        deployBridges();
	        
	        long endTime = System.currentTimeMillis();
	        log.info("Server startup complete. ("+(endTime-startTime)+" ms)");
	        
	        started = true;
	        startupTime = System.currentTimeMillis();
	        	        
	        return true;
    	}
    	catch (JMSException e)
    	{
    		if (e.getLinkedException() != null)
        	{
        		log.error("Server startup failed",e);
        		log.error("Original error was :",e.getLinkedException());
        	}
        	else
        		log.error("Server startup failed",e);
    		
    		return false;
    	}
    	catch (Exception e)
    	{
    		log.error("Server startup failed",e);
    		return false;
    	}
    }
    
    private void deployBridges() throws JMSException
    {
        if (bridgeDefinitionProvider != null)
        {
            // Load definitions
            this.bridgeDefinitionProvider.loadExistingDefinitions();

            BridgeDefinition[] bridgeDefs = bridgeDefinitionProvider.getBridgeDefinitions();
            for (int i = 0; i < bridgeDefs.length; i++)
            {
                log.debug("Deploying JMS bridge : "+bridgeDefs[i].getName());
                JMSBridge bridge = new JMSBridge(bridgeDefs[i]);
                if (bridgeDefs[i].isEnabled())
                    bridge.start();
                else
                    log.debug("JMS bridge is disabled : "+bridgeDefs[i].getName()); 
                bridges.add(bridge);
                bridgeDeployed(bridge);
            }
        }
    }
    
    private void undeployBridges()
    {
        for (int i = 0; i < bridges.size(); i++)
        {
            JMSBridge bridge = bridges.get(i);
            
            log.debug("Undeploying JMS bridge : "+bridge.getBridgeDefinition().getName());
            if (bridge.isStarted())
                bridge.stop();
            bridgeUndeployed(bridge);
        }
        bridges.clear();
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.FFMQServerMBean#shutdown()
     */
    @Override
	public synchronized boolean shutdown()
    {
    	if (!started)
    		return false;
    	
    	try
    	{
	        log.info("Server shutting down ...");
	        
	        // Undeploy bridges
	        undeployBridges();
	        
	        // Stop the tcp listener
	        if (tcpListener != null)
	        {
	        	tcpListener.stop();
	        	tcpListener = null;
	        }
	        
	        // Terminate the admin thread
	        if (adminThread != null)
	        {
	            adminThread.pleaseStop();
	            try
	            {
	                adminThread.join(ADMIN_THREAD_STOP_TIMEOUT*1000);
	                adminThread = null;
	            }
	            catch (InterruptedException e)
	            {
	                log.warn("Shutdown was interrupted while waiting for the admin thread to stop");
	            }
	        }
	        
	        // Undeploy engine
	        engine.undeploy();
	        try
	    	{
	    		if (jmxAgent != null)
	    			jmxAgent.unregister(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engine.getName()));
	    	}
	    	catch (Exception e)
	    	{
	    		log.error("Cannot unregister local engine from JMX agent",e);
	    	}
	        
	        // Undeploy JMX
	        if (jmxAgent != null)
	        	jmxAgent.stop();
	        
	        started = false;
	        	        
	        log.info("Shutdown complete.");
	        
	        return true;
    	}
    	catch (Exception e)
    	{
    		log.error("Server shutdown failed",e);
    		return false;
    	}
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.FFMQServerMBean#isStarted()
     */
    @Override
	public synchronized boolean isStarted()
    {
        return started;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.LocalEngineListener#engineDeployed()
     */
    @Override
	public void engineDeployed()
    {
        try
        {
            if (jmxAgent != null)
            {
                jmxAgent.register(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engine.getName()+",children=async-managers,name=notification"), engine.getNotificationAsyncTaskManager());
                jmxAgent.register(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engine.getName()+",children=async-managers,name=delivery"), engine.getDeliveryAsyncTaskManager());
                jmxAgent.register(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engine.getName()+",children=async-managers,name=disk-io"), engine.getDiskIOAsyncTaskManager());
            }
        }
        catch (Exception e)
        {
            log.error("Cannot register local engine async managers from JMX agent",e);
        }
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.LocalEngineListener#engineUndeployed()
     */
    @Override
	public void engineUndeployed()
    {
        try
        {
            if (jmxAgent != null)
            {
                jmxAgent.unregister(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engine.getName()+",children=async-managers,name=notification"));
                jmxAgent.unregister(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engine.getName()+",children=async-managers,name=delivery"));
                jmxAgent.unregister(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engine.getName()+",children=async-managers,name=disk-io"));
            }
        }
        catch (Exception e)
        {
            log.error("Cannot unregister local engine async managers from JMX agent",e);
        }
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.LocalEngineListener#queueDeployed(net.timewalker.ffmq4.local.destination.LocalQueue)
     */
    @Override
	public void queueDeployed(LocalQueue queue)
    {
    	try
    	{
    		if (jmxAgent != null)
    			jmxAgent.register(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engine.getName()+",children=queues,name="+queue.getName()), queue);
    	}
    	catch (Exception e)
    	{
    		log.error("Cannot register local queue on JMX agent",e);
    	}
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.LocalEngineListener#queueUndeployed(net.timewalker.ffmq4.local.destination.LocalQueue)
     */
    @Override
	public void queueUndeployed(LocalQueue queue)
    {
    	try
    	{
    		if (jmxAgent != null)
    			jmxAgent.unregister(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engine.getName()+",children=queues,name="+queue.getName()));
    	}
    	catch (Exception e)
    	{
    		log.error("Cannot unregister local queue from JMX agent",e);
    	}
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.LocalEngineListener#topicDeployed(net.timewalker.ffmq4.local.destination.LocalTopic)
     */
    @Override
	public void topicDeployed(LocalTopic topic)
    {
    	try
    	{
    		if (jmxAgent != null)
    			jmxAgent.register(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engine.getName()+",children=topics,name="+topic.getName()), topic);
    	}
    	catch (Exception e)
    	{
    		log.error("Cannot register local topic on JMX agent",e);
    	}
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.local.LocalEngineListener#topicUndeployed(net.timewalker.ffmq4.local.destination.LocalTopic)
     */
    @Override
	public void topicUndeployed(LocalTopic topic)
    {
    	try
    	{
    		if (jmxAgent != null)
    			jmxAgent.unregister(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Engines,engine="+engine.getName()+",children=topics,name="+topic.getName()));
    	}
    	catch (Exception e)
    	{
    		log.error("Cannot unregister local topic from JMX agent",e);
    	}
    }

    private void bridgeDeployed(JMSBridge bridge)
    {
    	try
    	{
    		if (jmxAgent != null)
    			jmxAgent.register(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Bridges,bridge="+bridge.getName()), bridge);
    	}
    	catch (Exception e)
    	{
    		log.error("Cannot register bridge on JMX agent",e);
    	}
    }

    private void bridgeUndeployed(JMSBridge bridge)
    {
    	try
    	{
    		if (jmxAgent != null)
    			jmxAgent.unregister(new ObjectName(JMXAgent.JMX_DOMAIN+":type=Bridges,bridge="+bridge.getName()));
    	}
    	catch (Exception e)
    	{
    		log.error("Cannot unregister bridge fromJMX agent",e);
    	}
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.FFMQServerMBean#isRemoteAdministrationEnabled()
     */
    @Override
	public synchronized boolean isRemoteAdministrationEnabled()
    {
    	return adminThread != null;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.FFMQServerMBean#getUptime()
     */
    @Override
	public synchronized long getUptime()
    {
    	return started ? System.currentTimeMillis()-startupTime : 0;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
	public synchronized void run()
    {
        if (!start())
        	return;
        
        try
        {
        	inRunnableMode = true;
        	while (!stopRequired)
        		wait();
        }
        catch (InterruptedException e)
        {
        	log.error("Server was interrupted.");
        }
        
        shutdown();      
    }
    
    public synchronized void pleaseStop()
    {
    	stopRequired = true;
    	notify();
    }
    
    /**
	 * @return the inRunnableMode
	 */
	public synchronized boolean isInRunnableMode()
	{
		return inRunnableMode;
	}
}
