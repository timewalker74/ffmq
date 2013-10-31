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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * FFMQVersion
 */
public class FFMQVersion
{
	private static final Log log = LogFactory.getLog(FFMQVersion.class);
	
    // Provider version
	private static int PROVIDER_MAJOR_VERSION  = 0;
	private static int PROVIDER_MINOR_VERSION  = 0;
	private static String PROVIDER_RELEASE_VERSION = "devel";
    
    // Supported JMS level
	private static final int JMS_MAJOR_VERSION = 1;
	private static final int JMS_MINOR_VERSION = 1;
    
    static {
    	try
    	{
	    	InputStream in = FFMQVersion.class.getClassLoader().getResourceAsStream("net/timewalker/ffmq4/FFMQ.version");
	    	if (in != null)
	    	{
	    		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	    		String versionString = reader.readLine();
	    		reader.close();
	    		if (versionString != null && !versionString.startsWith("$"))
	    		{
	    			StringTokenizer st = new StringTokenizer(versionString,".");
	    			int majorVersion = Integer.parseInt(st.nextToken());
	    			int minorVersion = Integer.parseInt(st.nextToken());
	    			String releaseVersion = st.nextToken();
	    			
	    			PROVIDER_MAJOR_VERSION = majorVersion;
	    			PROVIDER_MINOR_VERSION = minorVersion;
	    			PROVIDER_RELEASE_VERSION = releaseVersion;
	    		}
	    	}
    	}
    	catch (Exception e)
    	{
    		log.warn("Could not retrieve FFMQ version information : "+e.toString());
    	}
    }
    
    /**
     * Get the major version number of the provider
     * @return the major version number of the provider
     */
    public static int getProviderMajorVersion()
    {
    	return PROVIDER_MAJOR_VERSION;
    }
    
    /**
     * Get the minor version number of the provider
     * @return the minor version number of the provider
     */
    public static int getProviderMinorVersion()
    {
    	return PROVIDER_MINOR_VERSION;
    }
    
    /**
     * Get the release version string of the provider
     * @return the minor version string of the provider
     */
    public static String getProviderReleaseVersion()
    {
    	return PROVIDER_RELEASE_VERSION;
    }
    
    /**
     * Get the minor version number of the supported JMS specification
     * @return the minor version number of the supported JMS specification
     */
    public static int getJMSMajorVersion()
    {
    	return JMS_MAJOR_VERSION;
    }
    
    /**
     * Get the major version number of the supported JMS specification
     * @return the major version number of the supported JMS specification
     */
    public static int getJMSMinorVersion()
    {
    	return JMS_MINOR_VERSION;
    }
    
}
