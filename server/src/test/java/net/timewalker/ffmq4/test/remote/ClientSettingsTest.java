/* 
 * ===================================================================
 * This document and/or file is OVERKIZ property. All information
 * it contains is strictly confidential. This document and/or file
 * shall not be used, reproduced or passed on in any way, in full
 * or in part without OVERKIZ prior written approval.
 * All rights reserved.
 * ===================================================================
 */
package net.timewalker.ffmq4.test.remote;

import junit.framework.TestCase;
import net.timewalker.ffmq4.client.ClientEnvironment;

/**
 * ClientSettingsTest
 */
public class ClientSettingsTest extends TestCase
{
	public void testDefaultSettings()
	{
		assertEquals("SSLv3",ClientEnvironment.getSettings().getStringProperty("transport.tcp.ssl.protocol"));
		assertTrue(ClientEnvironment.getSettings().getBooleanProperty("consumer.sendAcksAsync", false));
		// Missing key
		assertNull(ClientEnvironment.getSettings().getStringProperty("bad.key"));
	}
	
	public void testSystemPropertyOverride()
	{
		assertEquals("SSLv3",ClientEnvironment.getSettings().getStringProperty("transport.tcp.ssl.protocol"));
		System.setProperty("ffmq4.transport.tcp.ssl.protocol", "TLS");
		assertEquals("TLS",ClientEnvironment.getSettings().getStringProperty("transport.tcp.ssl.protocol"));
		System.clearProperty("ffmq4.transport.tcp.ssl.protocol");
		assertEquals("SSLv3",ClientEnvironment.getSettings().getStringProperty("transport.tcp.ssl.protocol"));
		
		assertEquals(30,ClientEnvironment.getSettings().getIntProperty("transport.timeout",-1));
		System.setProperty("ffmq4.transport.timeout", "20");
		assertEquals(20,ClientEnvironment.getSettings().getIntProperty("transport.timeout",-1));
		System.clearProperty("ffmq4.transport.timeout");
		assertEquals(30,ClientEnvironment.getSettings().getIntProperty("transport.timeout",-1));
		
		assertTrue(ClientEnvironment.getSettings().getBooleanProperty("consumer.sendAcksAsync", false));
		System.setProperty("ffmq4.consumer.sendAcksAsync","false");
		assertFalse(ClientEnvironment.getSettings().getBooleanProperty("consumer.sendAcksAsync", true));
		System.clearProperty("ffmq4.consumer.sendAcksAsync");
		assertTrue(ClientEnvironment.getSettings().getBooleanProperty("consumer.sendAcksAsync", false));
	}
}
