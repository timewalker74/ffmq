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

/**
 * FFMQClientSettings
 */
public final class FFMQClientSettings
{
    // Transport related
    public static final String TRANSPORT_TIMEOUT = "transport.timeout";
    public static final String TRANSPORT_TCP_CONNECT_TIMEOUT = "transport.tcp.connectTimeout";
    public static final String TRANSPORT_TCP_SSL_PROTOCOL = "transport.tcp.ssl.protocol";
    public static final String TRANSPORT_TCP_SSL_IGNORE_CERTS = "transport.tcp.ssl.ignoreCertificates";
    
    // Consumer related
    public static final String CONSUMER_SEND_ACKS_ASYNC = "consumer.sendAcksAsync";
    
    // Producer related
    public static final String PRODUCER_RETRY_ON_QUEUE_FULL = "producer.retryOnQueueFull";
    public static final String PRODUCER_RETRY_TIMEOUT = "producer.retryTimeout";
}
