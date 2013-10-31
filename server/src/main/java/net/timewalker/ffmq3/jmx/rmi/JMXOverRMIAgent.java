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
package net.timewalker.ffmq3.jmx.rmi;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIServerSocketFactory;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.jmx.JMXAgent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JMXAgent
 */
public final class JMXOverRMIAgent implements JMXAgent
{
    private static final Log log = LogFactory.getLog(JMXOverRMIAgent.class);
    
    // Attributes
    private String agentName;
    private int jndiRmiPort;
    private String rmiListenAddr;
    
    // Runtime
    private MBeanServer mBeanServer;
    private JMXConnectorServer connectorServer;
    private JMXOverRMIServerSocketFactory mBeanServerSocketFactory;
    private Registry registry;
    private static boolean mx4jInitialized = false;
    
    /**
     * Constructor
     */
    public JMXOverRMIAgent( String agentName , int jndiRmiPort , String rmiListenAddr ) throws JMSException
    {
    	this.agentName = agentName;
    	this.jndiRmiPort = jndiRmiPort;
    	this.rmiListenAddr = rmiListenAddr;
        init();
    }
    
    /**
     * One-shot MX4J initialization
     */
    private static synchronized void initMX4J()
    {
        if (mx4jInitialized)
            return;
        
        LogFactory.getLog("javax.management");
        try
        {
        	// Soft dependency on MX4J
        	//-------------------------
        	// Execute the following code using introspection :
        	//   mx4j.log.Log.redirectTo(new Log4JLogger());
        	Class<?> logClass = Class.forName("mx4j.log.Log");
        	Class<?> loggerClass = Class.forName("mx4j.log.Logger");
        	Class<?> log4JLoggerClass = Class.forName("mx4j.log.Log4JLogger");
        	Object logger = log4JLoggerClass.newInstance();
        	Method redirectToMethod = logClass.getMethod("redirectTo", new Class[] { loggerClass });
        	redirectToMethod.invoke(null, new Object[] { logger });
        }
        catch (Exception e)
        {
        	log.debug("mx4j not available : "+e.toString());
        }
        
        mx4jInitialized = true;
    }
    
    private void init() throws JMSException
    {
        try
        {
            log.info("Starting JMX agent");

            // Setup mx4j
            initMX4J();
            
            // Get or create an RMI registry
            if (rmiListenAddr == null || rmiListenAddr.equals("auto"))
                rmiListenAddr = InetAddress.getLocalHost().getHostName();
            
            // Connector JNDI name
            String jndiName = "jmxconnector-"+agentName;
            
            try
            {
                registry = LocateRegistry.getRegistry(rmiListenAddr,jndiRmiPort);
                registry.lookup(jndiName);
                
                // Remove the old registered connector
                registry.unbind(jndiName);
                
                log.debug("RMI registry found at "+rmiListenAddr+":"+jndiRmiPort+" with connector already registered");
            }
            catch (NotBoundException e)
            {
                // Registry already exists
                log.debug("RMI registry found at "+rmiListenAddr+":"+jndiRmiPort);
            }
            catch (RemoteException e)
            {
                log.debug("Creating RMI registry at "+rmiListenAddr+":"+jndiRmiPort);
                RMIServerSocketFactory ssf = new JMXOverRMIServerSocketFactory(10,rmiListenAddr,false);
                registry = LocateRegistry.createRegistry(jndiRmiPort,null,ssf);
            }
            
            // Get the JVM MBean server
            mBeanServer = ManagementFactory.getPlatformMBeanServer();
            
            // Service URL
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://"+rmiListenAddr+"/jndi/rmi://"+rmiListenAddr+":" + jndiRmiPort + "/"+ jndiName);
            log.info("JMX Service URL : "+url);
            
            // Create and start the RMIConnectorServer
            Map<String,Object> env = new HashMap<>();
            mBeanServerSocketFactory = new JMXOverRMIServerSocketFactory(10,rmiListenAddr,true);
            env.put(RMIConnectorServer.JNDI_REBIND_ATTRIBUTE, "true");
            //env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, new JMXRMIClientSocketFactory(rmiListenAddr));
            env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, mBeanServerSocketFactory);
            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mBeanServer);
            connectorServer.start();
        }
        catch (Exception e)
        {
            throw new FFMQException("Could not initialize JMX agent","JMX_ERROR",e);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.jmx.JMXAgent#stop()
     */
    @Override
	public void stop()
    {
        log.info("Stopping JMX agent");
        if (connectorServer != null)
        {
            try
            {
                connectorServer.stop();
            }
            catch (Exception e)
            {
                log.error("Could not stop JMX connector server",e);
            }
            finally
            {
            	connectorServer = null;
            }
        }
        if (registry != null)
        {
	        try
	        {
	        	String jndiName = "jmxconnector-"+agentName;
	            registry.unbind(jndiName);
	        }
	        catch (Exception e)
	        {
	        	// Ignore
	        }
	        finally
	        {
	        	registry = null;
	        }
        }
        if (mBeanServerSocketFactory != null)
        {
        	try
            {
        		mBeanServerSocketFactory.close();
            }
        	catch (Exception e)
            {
                log.error("Could not close MBeans server socket factory",e);
            }
        	finally
            {
        		mBeanServerSocketFactory = null;
            }
        }
        mBeanServer = null;
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.jmx.JMXAgent#register(javax.management.ObjectName, java.lang.Object)
     */
	@Override
	public void register( ObjectName name , Object mBean ) throws JMSException
	{
		log.debug("Registering object " + name);
		try
		{
			this.mBeanServer.registerMBean(mBean, name);
		}
		catch (Exception e)
		{
			throw new FFMQException("Cannot register MBean", "JMX_ERROR", e);
		}
	}
  
  	/*
  	 * (non-Javadoc)
  	 * @see net.timewalker.ffmq3.jmx.JMXAgent#unregister(javax.management.ObjectName)
  	 */
	@Override
	public void unregister( ObjectName name ) throws JMSException
	{
	    log.debug("Unregistering object "+name);
	    try
	    {
	        this.mBeanServer.unregisterMBean(name);
	    }
	    catch (Exception e)
	    {
	        throw new FFMQException("Cannot unregister MBean","JMX_ERROR",e);
	    }
	}
}
