/* 
 * ===================================================================
 * This document and/or file is OVERKIZ property. All information
 * it contains is strictly confidential. This document and/or file
 * shall not be used, reproduced or passed on in any way, in full
 * or in part without OVERKIZ prior written approval.
 * All rights reserved.
 * ===================================================================
 */
package net.timewalker.ffmq4.jmx.platform;

import net.timewalker.ffmq4.jmx.AbstractJMXAgent;

/**
 * PlatformJMXAgent
 * @author spognant
 */
public final class PlatformJMXAgent extends AbstractJMXAgent
{
    /**
     * Constructor
     */
    public PlatformJMXAgent()
    {
        super();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.jmx.AbstractJMXAgent#getType()
     */
    @Override
    protected String getType()
    {
    	return "platform-only";
    }
}
