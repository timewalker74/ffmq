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
package net.timewalker.ffmq3.utils;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * JNDITools
 */
public class JNDITools
{
    private static Context createJndiContext(Hashtable env) throws NamingException
    {
        if (env == null)
        {
            /* Use jvm context */
            return new InitialContext();
        }
        else
        {
            return new InitialContext(env);
        }
    }
    
    /**
     * Create a JNDI context for the current provider
     * @param jdniInitialContextFactoryName
     * @param providerURL
     * @param extraEnv
     * @return a JNDI context
     * @throws NamingException
     */
    public static Context getContext( String jdniInitialContextFactoryName , String providerURL , Hashtable extraEnv ) throws NamingException
    {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, jdniInitialContextFactoryName);
        env.put(Context.PROVIDER_URL, providerURL);
        if (extraEnv != null)
            env.putAll(extraEnv);
        return createJndiContext(env);
    }
}
