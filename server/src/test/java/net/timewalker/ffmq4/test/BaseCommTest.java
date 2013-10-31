package net.timewalker.ffmq4.test;

import javax.jms.Session;

import net.timewalker.ffmq4.test.utils.CommTestParameters;

/**
 * BaseCommTestw
 */
public abstract class BaseCommTest extends AbstractCommTest
{
    public void testAAAWarmup() throws Exception
    {
        String testLabel = (isRemote() ? "Remote":"Local")+" "+(isTopicTest() ? "Topic" : "Queue")+
                            " "+(isListenerTest() ? "Listener" : "Receiver")+" Test ("+TestUtils.VOLUME_TEST_SIZE+" messages of size "+TestUtils.VOLUME_TEST_MSGSIZE+")"; 
        System.out.println("-----------------------------------------------------------------");
        System.out.println("  "+testLabel);
        System.out.println("-----------------------------------------------------------------");
        
        doTest(new CommTestParameters(10,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,false, 10,false,Session.AUTO_ACKNOWLEDGE,"TEST1"));
    }
    
    // SENDER NOT TRANSACTED RECEIVER AUTO ACK
    public void testOneSenderMultipleReceivers_SenderNotTransactedReceiverAutoAck() throws Exception
    {
        doTest(new CommTestParameters(1,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,false, 10,false,Session.AUTO_ACKNOWLEDGE,"TEST1"));
    }
    
    public void testOneSenderOneReceiver_SenderNotTransactedReceiverAutoAck() throws Exception
    {
        doTest(new CommTestParameters(1,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,false, 1,false,Session.AUTO_ACKNOWLEDGE,"TEST1"));
    }
    
    public void testMultipleSenderOneReceiver_SenderNotTransactedReceiverAutoAck() throws Exception
    {
        doTest(new CommTestParameters(10,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,false, 1,false,Session.AUTO_ACKNOWLEDGE,"TEST1"));
    }
    
    public void testMultipleSenderMultipleReceivers_SenderNotTransactedReceiverAutoAck() throws Exception
    {
        doTest(new CommTestParameters(10,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,false, 10,false,Session.AUTO_ACKNOWLEDGE,"TEST1"));
    }
    
    // SENDER TRANSACTED RECEIVER AUTO ACK
    public void testOneSenderOneReceiver_SenderTransactedReceiverAutoAck() throws Exception
    {
        doTest(new CommTestParameters(1,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,true, 1,false,Session.AUTO_ACKNOWLEDGE,"TEST1"));
    }
    
    public void testOneSenderMultipleReceivers_SenderTransactedReceiverAutoAck() throws Exception
    {
        doTest(new CommTestParameters(1,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,true, 10,false,Session.AUTO_ACKNOWLEDGE,"TEST1"));
    }
    
    public void testMultipleSenderOneReceiver_SenderTransactedReceiverAutoAck() throws Exception
    {
        doTest(new CommTestParameters(10,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,true, 1,false,Session.AUTO_ACKNOWLEDGE,"TEST1"));
    }
    
    public void testMultipleSenderMultipleReceivers_SenderTransactedReceiverAutoAck() throws Exception
    {
        doTest(new CommTestParameters(10,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,true, 10,false,Session.AUTO_ACKNOWLEDGE,"TEST1"));
    }
    
    // SENDER NOT TRANSACTED RECEIVER CLIENT ACK
    public void testOneSenderMultipleReceivers_SenderNotTransactedReceiverClientAck() throws Exception
    {
        doTest(new CommTestParameters(1,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,false, 10,false,Session.CLIENT_ACKNOWLEDGE,"TEST1"));
    }
    
    public void testMultipleSenderOneReceiver_SenderNotTransactedReceiverClientAck() throws Exception
    {
        doTest(new CommTestParameters(10,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,false, 1,false,Session.CLIENT_ACKNOWLEDGE,"TEST1"));
    }
    
    public void testMultipleSenderMultipleReceivers_SenderNotTransactedReceiverClientAck() throws Exception
    {
        doTest(new CommTestParameters(10,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,false, 10,false,Session.CLIENT_ACKNOWLEDGE,"TEST1"));
    }
    
    public void testOneSenderOneReceiver_SenderNotTransactedReceiverClientAck() throws Exception
    {
        doTest(new CommTestParameters(1,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,false, 1,false,Session.CLIENT_ACKNOWLEDGE,"TEST1"));
    }

    // SENDER TRANSACTED RECEIVER CLIENT ACK
    public void testOneSenderMultipleReceivers_SenderTransactedReceiverClientAck() throws Exception
    {
        doTest(new CommTestParameters(1,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,true, 10,false,Session.CLIENT_ACKNOWLEDGE,"TEST1"));
    }
    
    public void testMultipleSenderOneReceiver_SenderTransactedReceiverClientAck() throws Exception
    {
        doTest(new CommTestParameters(10,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,true, 1,false,Session.CLIENT_ACKNOWLEDGE,"TEST1"));
    }
    
    public void testMultipleSenderMultipleReceivers_SenderTransactedReceiverClientAck() throws Exception
    {
        doTest(new CommTestParameters(10,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,true, 10,false,Session.CLIENT_ACKNOWLEDGE,"TEST1"));
    }
    
    public void testOneSenderOneReceiver_SenderTransactedReceiverClientAck() throws Exception
    {
        doTest(new CommTestParameters(1,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,true, 1,false,Session.CLIENT_ACKNOWLEDGE,"TEST1"));
    }
    
    // SENDER NOT TRANSACTED RECEIVER TRANSACTED
    public void testOneSenderOneReceiver_SenderNotTransactedReceiverTransacted() throws Exception
    {
        doTest(new CommTestParameters(1,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,false, 1,true,Session.SESSION_TRANSACTED,"TEST1"));
    }
    
    public void testOneSenderMultipleReceivers_SenderNotTransactedReceiverTransacted() throws Exception
    {
        doTest(new CommTestParameters(1,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,false, 10,true,Session.SESSION_TRANSACTED,"TEST1"));
    }
    
    public void testMultipleSendersOneReceiver_SenderNotTransactedReceiverTransacted() throws Exception
    {
        doTest(new CommTestParameters(10,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,false, 1,true,Session.SESSION_TRANSACTED,"TEST1"));
    }

    public void testMultipleSendersMultipleReceivers_SenderNotTransactedReceiverTransacted() throws Exception
    {
        doTest(new CommTestParameters(10,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,false, 10,true,Session.SESSION_TRANSACTED,"TEST1"));
    }
    
    //  ALL TRANSACTED
    public void testOneSenderOneReceiver_AllTransacted() throws Exception
    {
        doTest(new CommTestParameters(1,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,true, 1,true,Session.SESSION_TRANSACTED,"TEST1"));
    }
    
    public void testOneSenderMultipleReceivers_AllTransacted() throws Exception
    {
        doTest(new CommTestParameters(1,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,true, 10,true,Session.SESSION_TRANSACTED,"TEST1"));
    }
    
    public void testMultipleSendersOneReceiver_AllTransacted() throws Exception
    {
        doTest(new CommTestParameters(10,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,true, 1,true,Session.SESSION_TRANSACTED,"TEST1"));
    }

    public void testMultipleSendersMultipleReceivers_AllTransacted() throws Exception
    {
        doTest(new CommTestParameters(10,TestUtils.VOLUME_TEST_SIZE,TestUtils.VOLUME_TEST_MSGSIZE,0,0,TestUtils.DELIVERY_MODE,TestUtils.PRIORITY,0,true, 10,true,Session.SESSION_TRANSACTED,"TEST1"));
    }
}
