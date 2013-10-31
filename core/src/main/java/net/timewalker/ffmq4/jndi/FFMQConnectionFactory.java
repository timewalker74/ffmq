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
package net.timewalker.ffmq4.jndi;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

import net.timewalker.ffmq4.FFMQConstants;
import net.timewalker.ffmq4.FFMQException;
import net.timewalker.ffmq4.local.FFMQEngine;
import net.timewalker.ffmq4.remote.connection.RemoteConnection;
import net.timewalker.ffmq4.transport.PacketTransportType;

/**
 * <p>Implementation of a JMS {@link ConnectionFactory}</p>
 */
public class FFMQConnectionFactory implements ConnectionFactory, Serializable, Referenceable
{
	private static final long serialVersionUID = 1L;
	
	// Attributes
	protected Hashtable<String,Object> environment;
	protected String clientID;
    
	/**
	 * Constructor
	 */
	public FFMQConnectionFactory()
	{
		this(new Hashtable<String,Object>());
	}
	
    /**
     * Constructor
     */
    public FFMQConnectionFactory( Hashtable<String, Object>  environment )
    {
        this.environment = environment;
        this.clientID = getStringProperty(FFMQConstants.JNDI_ENV_CLIENT_ID,null);
    }
    
    protected final String getStringProperty( String propertyName , String defaultValue )
    {
        String propertyValue = (String)environment.get(propertyName);
        if (propertyValue == null)
            return defaultValue;
        
        return propertyValue;
    }
    
    protected final int getIntProperty( String propertyName , int defaultValue )
    {
        String propertyValue = (String)environment.get(propertyName);
        if (propertyValue == null)
            return defaultValue;
        
        try
        {
            return Integer.parseInt(propertyValue);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }
    
    public String getSecurityPrincipal()
    {
    	return getStringProperty(Context.SECURITY_PRINCIPAL,null);
    }
    
    public void setSecurityPrincipal( String securityPrincipal )
    {
    	if (securityPrincipal != null)
    		environment.put(Context.SECURITY_PRINCIPAL,securityPrincipal);
    	else
    		environment.remove(Context.SECURITY_PRINCIPAL);
    }
    
    public String getSecurityCredentials()
    {
    	return getStringProperty(Context.SECURITY_CREDENTIALS,null);
    }
    
    public void setSecurityCredentials( String securityCredentials )
    {
    	if (securityCredentials != null)
    		environment.put(Context.SECURITY_CREDENTIALS,securityCredentials);
    	else
    		environment.remove(Context.SECURITY_CREDENTIALS);
    }
    
    public String getProviderURL()
    {
    	return getStringProperty(Context.PROVIDER_URL,PacketTransportType.TCP+"://"+FFMQConstants.DEFAULT_SERVER_HOST+":"+FFMQConstants.DEFAULT_SERVER_PORT);
    }
    
    public void setProviderURL( String providerURL )
    {
    	if (providerURL != null)
    		environment.put(Context.PROVIDER_URL, providerURL);
    	else
    		environment.remove(Context.PROVIDER_URL);
    }
    
    /*
     * (non-Javadoc)
     * @see javax.jms.ConnectionFactory#createConnection()
     */
    @Override
	public final Connection createConnection() throws JMSException
    {
    	String username = getStringProperty(Context.SECURITY_PRINCIPAL,null);
    	String password = getStringProperty(Context.SECURITY_CREDENTIALS,null);
    	
        return createConnection(username,password);
    }

    /*
     * (non-Javadoc)
     * @see javax.jms.ConnectionFactory#createConnection(java.lang.String, java.lang.String)
     */
    @Override
	public final Connection createConnection(String userName, String password) throws JMSException
    {
        URI providerURL = getProviderURI();
        
        String scheme = providerURL.getScheme();
        if (scheme.equals(PacketTransportType.VM))
        {
            String engineName = providerURL.getHost();
            return FFMQEngine.getDeployedInstance(engineName).openConnection(userName, password, clientID);
        }
        else 
        if (scheme.equals(PacketTransportType.TCP) ||
            scheme.equals(PacketTransportType.TCPS) ||
            scheme.equals(PacketTransportType.TCPNIO))
        {
            return new RemoteConnection(providerURL, userName, password, clientID);
        }
        else
            throw new FFMQException("Unknown transport protocol : " + scheme,"INVALID_TRANSPORT_PROTOCOL");
    }
    
    /**
     * Lookup the provider URI
     */
    protected final URI getProviderURI() throws JMSException
    {
        String providerURL = getProviderURL();
        URI parsedURL;
        try
        {
            parsedURL = new URI(providerURL);
        }
        catch (URISyntaxException e)
        {
            throw new FFMQException("Malformed provider URL : "+providerURL,"INVALID_PROVIDER_URL");
        }
        if (!parsedURL.isAbsolute())
            throw new FFMQException("Invalid provider URL : "+providerURL,"INVALID_PROVIDER_URL");
        
        return parsedURL;
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Referenceable#getReference()
     */
    @Override
	public final Reference getReference() throws NamingException
    {
    	Reference ref = new Reference(getClass().getName(),JNDIObjectFactory.class.getName(),null);
    	ref.add(new StringRefAddr("providerURL",getProviderURL()));
    	if (clientID != null)
    		ref.add(new StringRefAddr("clientID",clientID));
    	
    	return ref;
    }
}
