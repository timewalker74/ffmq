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
package net.timewalker.ffmq4.utils.id;

import java.net.InetAddress;
import java.util.Random;

import net.timewalker.ffmq4.utils.random.MTRandom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * UUIDProvider
 */
public final class UUIDProvider
{
	private static final Log log = LogFactory.getLog(UUIDProvider.class);
	
    private static UUIDProvider instance = null;
    
    /**
     * Get the singleton instance
     */
    public static synchronized UUIDProvider getInstance()
    {
        if (instance == null)
            instance = new UUIDProvider();
        
        return instance;
    }
    
    //----------------------------------------------------------------------------
    
    private Random seed;
    private String fixedPart;
    
    /** 
     * Constructor
     */
    private UUIDProvider()
    {
        try
        {
            this.seed = new MTRandom();
         
            // Try to find localhost address
            byte[] ifBytes = null;
            InetAddress inetaddress = InetAddress.getLocalHost();
            ifBytes = inetaddress != null ? inetaddress.getAddress() : null;
            if (ifBytes == null)
            {
            	// Cannot determine local node address,
            	// use a random value instead
            	log.warn("Cannot determine localhost address, falling back to random value ...");
            	ifBytes = new byte[4];
            	seed.nextBytes(ifBytes);
            }
            
            StringBuffer base = new StringBuffer();
            
            String s = hexFormat(getInt(ifBytes));
            String s1 = hexFormat(hashCode());
            base.append("-");
            base.append(s.substring(0, 4));
            base.append("-");
            base.append(s.substring(4));
            base.append("-");
            base.append(s1.substring(0, 4));
            base.append("-");
            base.append(s1.substring(4));
            fixedPart = base.toString();
            seed.nextInt();
        }
        catch (Exception e)
        {
        	log.fatal("Could not initialise UUID generator",e);
            throw new IllegalStateException("Could not initialise UUID generator : "+e.getMessage());
        }
    }

    /**
     * Generate a new UUID
     */
    public String getUUID()
    {
        StringBuffer uuid = new StringBuffer(36);
        int i = (int)System.currentTimeMillis();
        int j = seed.nextInt();
        hexFormat(i,uuid);
        uuid.append(fixedPart);
        hexFormat(j,uuid);        
        return uuid.toString();
    }

    public String getShortUUID()
    {
    	StringBuffer uuid = new StringBuffer(16);
        int i = (int)System.currentTimeMillis();
        int j = seed.nextInt();
        hexFormat(i,uuid);
        hexFormat(j,uuid);        
        return uuid.toString();
    }
    
    private static int getInt(byte abyte0[])
    {
        int i = 0;
        int j = 24;
        for (int k = 0 ; j >= 0 ; k++)
        {
            int l = abyte0[k] & 0xff;
            i += l << j;
            j -= 8;
        }

        return i;
    }

    private static String hexFormat(int i)
    {
        StringBuffer sb = new StringBuffer(8);
        hexFormat(i,sb);
        return sb.toString();
    }
    
    private static void hexFormat(int i, StringBuffer uuid)
    {
        String s = Integer.toHexString(i);
        for (int n = 0 ; n < 8 - s.length() ; n++)
            uuid.append("0");
        uuid.append(s);
    }
}
