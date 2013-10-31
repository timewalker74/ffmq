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
package net.timewalker.ffmq3;

import net.timewalker.ffmq3.jndi.FFMQInitialContextFactory;

/**
 * FFMQConstants
 */
public interface FFMQConstants
{    
    // Network defaults
    public static final String DEFAULT_SERVER_HOST = "localhost";
    public static final int DEFAULT_SERVER_PORT = 10002;
    
    // Transport protocol version
    public static final int TRANSPORT_PROTOCOL_VERSION = 9;
    
    // JNDI related constants
    public static final String JNDI_CONTEXT_FACTORY = FFMQInitialContextFactory.class.getName();
    public static final String JNDI_CONNECTION_FACTORY_NAME = "factory/ConnectionFactory";
    public static final String JNDI_QUEUE_CONNECTION_FACTORY_NAME = "factory/QueueConnectionFactory";
    public static final String JNDI_TOPIC_CONNECTION_FACTORY_NAME = "factory/TopicConnectionFactory";
    public static final String JNDI_ENV_CLIENT_ID = "ffmq.naming.clientID";
    
    // Max name size
    public static final int MAX_QUEUE_NAME_SIZE = 128;
    public static final int MAX_TOPIC_NAME_SIZE = 196;
    
    // Administration queues
    public static final String ADM_REQUEST_QUEUE = "_FFMQ_ADM_REQUEST";
    public static final String ADM_REPLY_QUEUE   = "_FFMQ_ADM_REPLY";
}
