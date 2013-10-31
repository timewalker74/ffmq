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
package net.timewalker.ffmq3.transport.packet;

import net.timewalker.ffmq3.transport.packet.query.AcknowledgeQuery;
import net.timewalker.ffmq3.transport.packet.query.CloseBrowserEnumerationQuery;
import net.timewalker.ffmq3.transport.packet.query.CloseBrowserQuery;
import net.timewalker.ffmq3.transport.packet.query.CloseConsumerQuery;
import net.timewalker.ffmq3.transport.packet.query.CloseSessionQuery;
import net.timewalker.ffmq3.transport.packet.query.CommitQuery;
import net.timewalker.ffmq3.transport.packet.query.CreateBrowserQuery;
import net.timewalker.ffmq3.transport.packet.query.CreateConsumerQuery;
import net.timewalker.ffmq3.transport.packet.query.CreateDurableSubscriberQuery;
import net.timewalker.ffmq3.transport.packet.query.CreateSessionQuery;
import net.timewalker.ffmq3.transport.packet.query.CreateTemporaryQueueQuery;
import net.timewalker.ffmq3.transport.packet.query.CreateTemporaryTopicQuery;
import net.timewalker.ffmq3.transport.packet.query.DeleteTemporaryQueueQuery;
import net.timewalker.ffmq3.transport.packet.query.DeleteTemporaryTopicQuery;
import net.timewalker.ffmq3.transport.packet.query.GetQuery;
import net.timewalker.ffmq3.transport.packet.query.OpenConnectionQuery;
import net.timewalker.ffmq3.transport.packet.query.PingQuery;
import net.timewalker.ffmq3.transport.packet.query.PrefetchQuery;
import net.timewalker.ffmq3.transport.packet.query.PutQuery;
import net.timewalker.ffmq3.transport.packet.query.QueueBrowserFetchElementQuery;
import net.timewalker.ffmq3.transport.packet.query.QueueBrowserGetEnumerationQuery;
import net.timewalker.ffmq3.transport.packet.query.RecoverQuery;
import net.timewalker.ffmq3.transport.packet.query.RollbackMessageQuery;
import net.timewalker.ffmq3.transport.packet.query.RollbackQuery;
import net.timewalker.ffmq3.transport.packet.query.SetClientIDQuery;
import net.timewalker.ffmq3.transport.packet.query.StartConnectionQuery;
import net.timewalker.ffmq3.transport.packet.query.StopConnectionQuery;
import net.timewalker.ffmq3.transport.packet.query.UnsubscribeQuery;
import net.timewalker.ffmq3.transport.packet.response.AcknowledgeResponse;
import net.timewalker.ffmq3.transport.packet.response.CloseBrowserEnumerationResponse;
import net.timewalker.ffmq3.transport.packet.response.CloseBrowserResponse;
import net.timewalker.ffmq3.transport.packet.response.CloseConsumerResponse;
import net.timewalker.ffmq3.transport.packet.response.CloseSessionResponse;
import net.timewalker.ffmq3.transport.packet.response.CommitResponse;
import net.timewalker.ffmq3.transport.packet.response.CreateBrowserResponse;
import net.timewalker.ffmq3.transport.packet.response.CreateConsumerResponse;
import net.timewalker.ffmq3.transport.packet.response.CreateSessionResponse;
import net.timewalker.ffmq3.transport.packet.response.CreateTemporaryQueueResponse;
import net.timewalker.ffmq3.transport.packet.response.CreateTemporaryTopicResponse;
import net.timewalker.ffmq3.transport.packet.response.DeleteTemporaryQueueResponse;
import net.timewalker.ffmq3.transport.packet.response.DeleteTemporaryTopicResponse;
import net.timewalker.ffmq3.transport.packet.response.ErrorResponse;
import net.timewalker.ffmq3.transport.packet.response.GetResponse;
import net.timewalker.ffmq3.transport.packet.response.OpenConnectionResponse;
import net.timewalker.ffmq3.transport.packet.response.PingResponse;
import net.timewalker.ffmq3.transport.packet.response.PrefetchResponse;
import net.timewalker.ffmq3.transport.packet.response.PutResponse;
import net.timewalker.ffmq3.transport.packet.response.QueueBrowserFetchElementResponse;
import net.timewalker.ffmq3.transport.packet.response.QueueBrowserGetEnumerationResponse;
import net.timewalker.ffmq3.transport.packet.response.RecoverResponse;
import net.timewalker.ffmq3.transport.packet.response.RollbackMessageResponse;
import net.timewalker.ffmq3.transport.packet.response.RollbackResponse;
import net.timewalker.ffmq3.transport.packet.response.SetClientIDResponse;
import net.timewalker.ffmq3.transport.packet.response.StartConnectionResponse;
import net.timewalker.ffmq3.transport.packet.response.StopConnectionResponse;
import net.timewalker.ffmq3.transport.packet.response.UnsubscribeResponse;

/**
 * PacketType
 */
public final class PacketType
{
    public static final byte NOTIFICATION                = 1;
    public static final byte R_ERROR                     = 2;
    public static final byte Q_ACKNOWLEDGE               = 3;
    public static final byte R_ACKNOWLEDGE               = 4;
    public static final byte Q_CLOSE_SESSION             = 5;
    public static final byte R_CLOSE_SESSION             = 6;
    public static final byte Q_COMMIT                    = 7;
    public static final byte R_COMMIT                    = 8;
    public static final byte Q_CREATE_CONSUMER           = 9;
    public static final byte R_CREATE_CONSUMER           = 10;
    public static final byte Q_CREATE_DURABLE_SUBSCRIBER = 11;
    public static final byte Q_CREATE_SESSION            = 15;
    public static final byte R_CREATE_SESSION            = 16;
    public static final byte Q_CREATE_TEMP_QUEUE         = 17;
    public static final byte R_CREATE_TEMP_QUEUE         = 18;
    public static final byte Q_CREATE_TEMP_TOPIC         = 19;
    public static final byte R_CREATE_TEMP_TOPIC         = 20;
    public static final byte Q_DELETE_TEMP_QUEUE         = 21;
    public static final byte R_DELETE_TEMP_QUEUE         = 22;
    public static final byte Q_DELETE_TEMP_TOPIC         = 23;
    public static final byte R_DELETE_TEMP_TOPIC         = 24;
    public static final byte Q_GET                       = 25;
    public static final byte R_GET                       = 26;
    public static final byte Q_OPEN_CONNECTION           = 27;
    public static final byte R_OPEN_CONNECTION           = 28;
    public static final byte Q_PUT                       = 29;
    public static final byte R_PUT                       = 30;
    public static final byte Q_RECOVER                   = 31;
    public static final byte R_RECOVER                   = 32;
    public static final byte Q_ROLLBACK                  = 33;
    public static final byte R_ROLLBACK                  = 34;
    public static final byte Q_SET_CLIENT_ID             = 35;
    public static final byte R_SET_CLIENT_ID             = 36;
    public static final byte Q_START_CONNECTION          = 37;
    public static final byte R_START_CONNECTION          = 38;
    public static final byte Q_STOP_CONNECTION           = 39;
    public static final byte R_STOP_CONNECTION           = 40;
    public static final byte Q_CREATE_BROWSER            = 41;
    public static final byte R_CREATE_BROWSER            = 42;
    public static final byte Q_CREATE_BROWSER_ENUM       = 43;
    public static final byte R_CREATE_BROWSER_ENUM       = 44;
    public static final byte Q_BROWSER_ENUM_FETCH        = 45;
    public static final byte R_BROWSER_ENUM_FETCH        = 46;
    public static final byte Q_CLOSE_BROWSER             = 47;
    public static final byte R_CLOSE_BROWSER             = 48;
    public static final byte Q_CLOSE_BROWSER_ENUM        = 49;
    public static final byte R_CLOSE_BROWSER_ENUM        = 50;
    public static final byte Q_CLOSE_CONSUMER            = 51;
    public static final byte R_CLOSE_CONSUMER            = 52;
    public static final byte Q_UNSUBSCRIBE               = 55;
    public static final byte R_UNSUBSCRIBE               = 56;
    public static final byte Q_PREFETCH                  = 57;
    public static final byte R_PREFETCH                  = 58;
    public static final byte Q_PING                      = 59;
    public static final byte R_PING                      = 60;
    public static final byte Q_ROLLBACK_MESSAGE          = 61;
    public static final byte R_ROLLBACK_MESSAGE          = 62;
    
    /**
     * Create a packet instance of the given type
     */
    public static AbstractPacket createInstance( byte type )
    {
        switch (type)
        {
            case NOTIFICATION:                return new NotificationPacket();
            case R_ERROR:                     return new ErrorResponse();
            case Q_ACKNOWLEDGE:               return new AcknowledgeQuery();
            case R_ACKNOWLEDGE:               return new AcknowledgeResponse();
            case Q_CLOSE_SESSION:             return new CloseSessionQuery();
            case R_CLOSE_SESSION:             return new CloseSessionResponse();
            case Q_COMMIT:                    return new CommitQuery();
            case R_COMMIT:                    return new CommitResponse();
            case Q_CREATE_CONSUMER:           return new CreateConsumerQuery();
            case R_CREATE_CONSUMER:           return new CreateConsumerResponse();
            case Q_CREATE_DURABLE_SUBSCRIBER: return new CreateDurableSubscriberQuery();
            case Q_CREATE_SESSION:            return new CreateSessionQuery();
            case R_CREATE_SESSION:            return new CreateSessionResponse();
            case Q_CREATE_TEMP_QUEUE:         return new CreateTemporaryQueueQuery();
            case R_CREATE_TEMP_QUEUE:         return new CreateTemporaryQueueResponse();
            case Q_CREATE_TEMP_TOPIC:         return new CreateTemporaryTopicQuery();
            case R_CREATE_TEMP_TOPIC:         return new CreateTemporaryTopicResponse();
            case Q_DELETE_TEMP_QUEUE:         return new DeleteTemporaryQueueQuery();
            case R_DELETE_TEMP_QUEUE:         return new DeleteTemporaryQueueResponse();
            case Q_DELETE_TEMP_TOPIC:         return new DeleteTemporaryTopicQuery();
            case R_DELETE_TEMP_TOPIC:         return new DeleteTemporaryTopicResponse();
            case Q_GET:                       return new GetQuery();
            case R_GET:                       return new GetResponse();
            case Q_OPEN_CONNECTION:           return new OpenConnectionQuery();
            case R_OPEN_CONNECTION:           return new OpenConnectionResponse();
            case Q_PUT:                       return new PutQuery();
            case R_PUT:                       return new PutResponse();
            case Q_RECOVER:                   return new RecoverQuery();
            case R_RECOVER:                   return new RecoverResponse();
            case Q_ROLLBACK:                  return new RollbackQuery();
            case R_ROLLBACK:                  return new RollbackResponse();
            case Q_SET_CLIENT_ID:             return new SetClientIDQuery();
            case R_SET_CLIENT_ID:             return new SetClientIDResponse();
            case Q_START_CONNECTION:          return new StartConnectionQuery();
            case R_START_CONNECTION:          return new StartConnectionResponse();
            case Q_STOP_CONNECTION:           return new StopConnectionQuery();
            case R_STOP_CONNECTION:           return new StopConnectionResponse();
            case Q_CREATE_BROWSER:            return new CreateBrowserQuery();
            case R_CREATE_BROWSER:            return new CreateBrowserResponse();
            case Q_CREATE_BROWSER_ENUM:       return new QueueBrowserGetEnumerationQuery();
            case R_CREATE_BROWSER_ENUM:       return new QueueBrowserGetEnumerationResponse();
            case Q_BROWSER_ENUM_FETCH:        return new QueueBrowserFetchElementQuery();
            case R_BROWSER_ENUM_FETCH:        return new QueueBrowserFetchElementResponse();
            case Q_CLOSE_BROWSER:             return new CloseBrowserQuery();
            case R_CLOSE_BROWSER:             return new CloseBrowserResponse();
            case Q_CLOSE_BROWSER_ENUM:        return new CloseBrowserEnumerationQuery();
            case R_CLOSE_BROWSER_ENUM:        return new CloseBrowserEnumerationResponse();
            case Q_CLOSE_CONSUMER:            return new CloseConsumerQuery();
            case R_CLOSE_CONSUMER:            return new CloseConsumerResponse();
            case Q_UNSUBSCRIBE:               return new UnsubscribeQuery();
            case R_UNSUBSCRIBE:               return new UnsubscribeResponse();
            case Q_PREFETCH:                  return new PrefetchQuery();
            case R_PREFETCH:                  return new PrefetchResponse();
            case Q_PING:                      return new PingQuery();
            case R_PING:                      return new PingResponse();
            case Q_ROLLBACK_MESSAGE:          return new RollbackMessageQuery();
            case R_ROLLBACK_MESSAGE:          return new RollbackMessageResponse();
            
            default:
                throw new IllegalArgumentException("Unsupported packet type : "+type);
        }
    }
}
