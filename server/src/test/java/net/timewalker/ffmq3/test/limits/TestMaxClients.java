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

package net.timewalker.ffmq3.test.limits;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;

import net.timewalker.ffmq3.FFMQConstants;
import net.timewalker.ffmq3.test.TestUtils;

/**
 * TestMaxClients
 */
public class TestMaxClients
{
	public static void main(String[] args) throws Exception
	{
		Hashtable<String,Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, FFMQConstants.JNDI_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, TestUtils.TCP_TRANSPORT_URI);
        Context context = new InitialContext(env);
        
        ConnectionFactory connFactory = (ConnectionFactory)context.lookup(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME);
        
        List<Connection> conns = new ArrayList<>();
        for(int n=0;n<1000;n++)
        {
	        Connection conn = connFactory.createConnection("test","test");
	        conn.start();
	        conns.add(conn);
	        System.out.println(n);
        }
        
        Thread.sleep(10*1000);
	}
}
