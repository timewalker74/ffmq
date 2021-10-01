/* 
 * ===================================================================
 * This document and/or file is OVERKIZ property. All information
 * it contains is strictly confidential. This document and/or file
 * shall not be used, reproduced or passed on in any way, in full
 * or in part without OVERKIZ prior written approval.
 * All rights reserved.
 * ===================================================================
 */
package net.timewalker.ffmq4.jmx;

import java.lang.management.ManagementFactory;

import javax.jms.JMSException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.timewalker.ffmq4.FFMQException;

/**
 * AbstractJMXAgent
 */
public abstract class AbstractJMXAgent implements JMXAgent
{
	protected final Log log = LogFactory.getLog(getClass());

    // Runtime
	protected final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer(); // Get the JVM MBean server
    
    /**
     * Constructor
     */
    public AbstractJMXAgent()
    {
        init();
    }
    
    protected abstract String getType(); 
    
    private void init()
    {
        log.info("Starting JMX agent ("+getType()+")");
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.jmx.JMXAgent#stop()
     */
    @Override
	public void stop()
    {
        log.info("Stopping JMX agent");
    }
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq4.jmx.JMXAgent#register(javax.management.ObjectName, java.lang.Object)
     */
	@Override
	public final void register( ObjectName name , Object mBean ) throws JMSException
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
  	 * @see net.timewalker.ffmq4.jmx.JMXAgent#unregister(javax.management.ObjectName)
  	 */
	@Override
	public final void unregister( ObjectName name ) throws JMSException
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
