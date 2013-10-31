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
package net.timewalker.ffmq3.listeners.tcp.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;

import javax.jms.JMSException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

import net.timewalker.ffmq3.FFMQCoreSettings;
import net.timewalker.ffmq3.FFMQException;
import net.timewalker.ffmq3.FFMQServerSettings;
import net.timewalker.ffmq3.jmx.JMXAgent;
import net.timewalker.ffmq3.listeners.ClientProcessor;
import net.timewalker.ffmq3.listeners.tcp.AbstractTcpClientListener;
import net.timewalker.ffmq3.local.FFMQEngine;
import net.timewalker.ffmq3.transport.PacketTransport;
import net.timewalker.ffmq3.transport.PacketTransportException;
import net.timewalker.ffmq3.transport.PacketTransportType;
import net.timewalker.ffmq3.transport.tcp.io.TcpPacketTransport;
import net.timewalker.ffmq3.utils.Settings;
import net.timewalker.ffmq3.utils.id.UUIDProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TcpNetworkServer
 */
public final class TcpListener extends AbstractTcpClientListener implements Runnable, TcpListenerMBean
{    
    private static final Log log = LogFactory.getLog(TcpListener.class);

    // Runtime
    private ServerSocket serverSocket;
    private Thread listenerThread;
    private boolean stopRequired = false;
    private boolean usingSSL;
    
    /**
     * Constructor
     */
    public TcpListener( FFMQEngine engine ,
                        String listenAddr , 
                        int port ,
                        Settings settings )
    {
        this(engine,listenAddr,port,settings,null);
    }
    
    /**
     * Constructor
     */
    public TcpListener( FFMQEngine engine ,
                        String listenAddr , 
                        int port ,
                        Settings settings ,
                        JMXAgent jmxAgent )
    {
        super(engine,settings,jmxAgent,listenAddr,port);
        this.usingSSL = settings.getBooleanProperty(FFMQCoreSettings.TRANSPORT_TCP_SSL_ENABLED, false);
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.listeners.AbstractListener#start()
     */
    public synchronized void start() throws JMSException
    {
    	if (started)
    		return;
    	
    	log.info("Starting listener ["+getName()+"]");
    	
    	stopRequired = false;
    	
    	initServerSocket();
    	listenerThread = new Thread(this,"FFMQ-TCP-Server-"+serverSocket.getLocalPort());
    	listenerThread.start();
    	
    	started = true;
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.listeners.Listener#getName()
     */
    public String getName()
    {
    	return (usingSSL ? PacketTransportType.TCPS : PacketTransportType.TCP)+"-"+listenAddr+"-"+listenPort;
    }
    
    private void initServerSocket() throws JMSException
	{
		try
        {
            InetAddress bindAddress = getBindAddress();            
            int tcpBackLog = settings.getIntProperty(FFMQServerSettings.LISTENER_TCP_BACK_LOG, DEFAULT_TCP_BACK_LOG);
            log.debug("TCP back log = "+tcpBackLog);
             
            serverSocket = createServerSocket(listenPort,tcpBackLog,bindAddress,usingSSL);
            serverSocket.setReuseAddress(true);
        }
        catch (JMSException e)
        {
            throw e;
        }
        catch (Exception e)
        {
        	throw new FFMQException("Could not initialize server socket","NETWORK_ERROR",e);
        }
	}
	
    private void closeServerSocket()
	{
		// Close the listen socket
        try
        {
            if (serverSocket != null)
                serverSocket.close();
        }
        catch (IOException e)
        {
            log.error("Could not close server socket",e);
        }
        finally
        {
        	serverSocket = null;
        }
	}
    
    /*
     * (non-Javadoc)
     * @see net.timewalker.ffmq3.utils.concurrent.SynchronizableThread#run()
     */
    public void run()
    {
        try
        {
            log.debug("Waiting for clients ["+getName()+"]");
            while (!stopRequired)
            {
                Socket clientSocket = serverSocket.accept();
                
                // Enforce listener capacity
                int activeClients = getActiveClients();
                if (activeClients >= listenerCapacity)
                {
                	log.warn("Listener is full (max="+listenerCapacity+"), dropping new connection attempt.");
                	try
                	{
                		clientSocket.close();
                	}
                	catch (Exception e)
                    {
                        log.error("Cannot close incoming connection",e);
                    }
                	continue;
                }
                
                String clientId = UUIDProvider.getInstance().getShortUUID();
                log.debug("Accepting a new client from "+clientSocket.getInetAddress().getHostAddress()+" ("+(activeClients+1)+") : "+clientId+" ["+getName()+"]");
                try
                {
                    ClientProcessor processor = createProcessor(clientId,clientSocket);
                    registerClient(processor);
                    processor.start();
                }
                catch (Exception e)
                {
                	try
                	{
                		clientSocket.close();
                	}
                	catch (Exception ex)
                	{
                		log.error("Could not close socket ["+getName()+"]",ex);
                	}
                	
                    log.error("Client failed : "+clientId+" ["+getName()+"]",e);
                }
            }
        }
        catch (Exception e)
        {
            if (!stopRequired)
                log.fatal("Server failed ["+getName()+"]",e);
        }
    }

    /**
     * Create a new processor
     */
    protected ClientProcessor createProcessor( String clientId , Socket clientSocket ) throws PacketTransportException
    {
    	PacketTransport transport = new TcpPacketTransport(clientId,clientSocket,settings);
    	ClientProcessor clientProcessor = new ClientProcessor(clientId,this,localEngine,transport);
    	return clientProcessor;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.listeners.AbstractListener#stop()
     */
    public synchronized void stop()
    {
    	if (!started)
    		return;
    	
    	log.info("Stopping listener ["+getName()+"]");
    	
        stopRequired = true;
        
        // Close the listen socket
        closeServerSocket();
        
        // Wait for listener thread to stop
        try
        {
        	if (listenerThread != null)
        		listenerThread.join();
        }
        catch (InterruptedException e)
        {
        	log.error("Wait for listener thread termination was interrupted");
        }
        finally
        {
        	listenerThread = null;
        }
        
        // Then stop remaining clients
        closeRemainingClients();
        
        started = false;
    }
    
    private ServerSocket createServerSocket( int port , int tcpBackLog , InetAddress localAddr , boolean useSSL ) throws JMSException
    {
        try
        {
            if (useSSL)
            {
                SSLServerSocket socket = (SSLServerSocket)createSSLContext().getServerSocketFactory().createServerSocket(port,tcpBackLog,localAddr);
                socket.setNeedClientAuth(false);
                return socket;
            }
            else
                return new ServerSocket(port,tcpBackLog,localAddr);
        }
        catch (Exception e)
        {
            throw new FFMQException("Cannot create server socket","NETWORK_ERROR",e);
        }
    }
    
    private SSLContext createSSLContext() throws JMSException
    {
        try
        {
            String sslProtocol = settings.getStringProperty(FFMQCoreSettings.TRANSPORT_TCP_SSL_PROTOCOL, "SSLv3");
            String keyManagerAlgorithm = settings.getStringProperty(FFMQCoreSettings.TRANSPORT_TCP_SSL_KEYMANAGER_ALGORITHM, "SunX509");
            String keyStoreType = settings.getStringProperty(FFMQCoreSettings.TRANSPORT_TCP_SSL_KEYSTORE_TYPE, "JKS");
            String keyStorePath = settings.getStringProperty(FFMQCoreSettings.TRANSPORT_TCP_SSL_KEYSTORE_PATH, "../conf/server-keystore.jks");
            String keyStorePass = settings.getStringProperty(FFMQCoreSettings.TRANSPORT_TCP_SSL_KEYSTORE_PASWORD, "ffmqpass");
            String keyPass = settings.getStringProperty(FFMQCoreSettings.TRANSPORT_TCP_SSL_KEYSTORE_KEY_PASSWORD, "ffmqpass");
            
            SSLContext sslContext = SSLContext.getInstance(sslProtocol);
            log.debug("Created an SSL context : protocol=["+sslContext.getProtocol()+"] provider=["+sslContext.getProvider()+"]");
            
            // Load available keys
            KeyManager[] keyManagers;
            File keyStoreFile = new File(keyStorePath);
            if (!keyStoreFile.canRead())
                throw new FFMQException("Cannot read keystore file : "+keyStoreFile.getAbsolutePath(),"FS_ERROR");
                
            KeyStore ks = KeyStore.getInstance(keyStoreType);
            log.debug("Created keystore : type=["+ks.getType()+"] provider=["+ks.getProvider()+"]");
            char ksPass[] = keyStorePass.toCharArray();
            char ctPass[] = keyPass.toCharArray();
            log.debug("Loading keystore from "+keyStoreFile.getAbsolutePath());
            InputStream kis = new FileInputStream(keyStoreFile); 
            ks.load(kis, ksPass);
            kis.close();
            
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(keyManagerAlgorithm);
            log.debug("Created KeyManagerFactory : algorithm=["+kmf.getAlgorithm()+"] provider=["+kmf.getProvider()+"]");
            log.debug("Initializing KeyManagerFactory with keystore ...");
            kmf.init(ks, ctPass);
            
            keyManagers = kmf.getKeyManagers();
            
            sslContext.init(keyManagers, null, null);
            
            return sslContext;
        }
        catch (JMSException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new FFMQException("Cannot create SSL context","NETWORK_ERROR",e);
        }
    }
}
