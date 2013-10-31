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

/**
 * FFMQAdminClientSettings
 */
public interface FFMQAdminClientSettings
{
    public static final String SERVER_TCP_HOST     = "adminClient.remoteServer.tcp.host";
    public static final String SERVER_TCP_PORT     = "adminClient.remoteServer.tcp.port";
    public static final String ADMIN_USER_NAME     = "adminClient.userName";
    public static final String ADMIN_USER_PASSWORD = "adminClient.password";
    
    public static final String ADMIN_REQUEST_TIMEOUT = "adminClient.request.timeout";
    
    public static final String ADM_COMMAND                 = "admin.command";
}
