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
 * FFMQAdminConstants
 */
public interface FFMQAdminConstants
{    
    public static final String ADM_HEADER_PREFIX        = "FFMQ_ADM_";
    public static final String ADM_HEADER_COMMAND       = ADM_HEADER_PREFIX+"adminCommand";
    public static final String ADM_HEADER_ERRMSG        = ADM_HEADER_PREFIX+"errorMessage";
    
    public static final String ADM_COMMAND_CREATE_QUEUE = "createQueue";
    public static final String ADM_COMMAND_CREATE_TOPIC = "createTopic";
    public static final String ADM_COMMAND_DELETE_QUEUE = "deleteQueue";
    public static final String ADM_COMMAND_DELETE_TOPIC = "deleteTopic";
    public static final String ADM_COMMAND_PURGE_QUEUE  = "purgeQueue";
    public static final String ADM_COMMAND_SHUTDOWN     = "shutdown";
    
    public static final String[] ADM_COMMAND_ALL = {
        ADM_COMMAND_CREATE_QUEUE,
        ADM_COMMAND_CREATE_TOPIC,
        ADM_COMMAND_DELETE_QUEUE,
        ADM_COMMAND_DELETE_TOPIC,
        ADM_COMMAND_PURGE_QUEUE,
        ADM_COMMAND_SHUTDOWN
    };
}
