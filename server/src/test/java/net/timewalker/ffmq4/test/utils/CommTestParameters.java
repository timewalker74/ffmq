package net.timewalker.ffmq4.test.utils;


/**
 * CommTestParameters
 */
public class CommTestParameters
{
    // Sender(s) settings
    public int senderCount;
    public int messageSize;
    public int messageCount;
    public int minDelay;
    public int maxDelay;
    public int deliveryMode;
    public int priority;
    public long timeToLive;
    public boolean senderTransacted;
    
    // Receiver(s) settings
    public int receiverCount;
    public boolean receiverTransacted;
    public int acknowledgeMode;
    
    // General settings
    public String destinationName;
    
    /**
     * Constructor
     */
    public CommTestParameters( int senderCount,
                               int messageCount,
                               int messageSize,
                               int minDelay,
                               int maxDelay,
                               int deliveryMode,
                               int priority,
                               long timeToLive,
                               boolean senderTransacted,
                               int receiverCount,
                               boolean receiverTransacted,
                               int acknowledgeMode,
                               String destinationName)
    {
        this.senderCount = senderCount;
        this.messageSize = messageSize;
        this.messageCount = messageCount;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.deliveryMode = deliveryMode;
        this.priority = priority;
        this.timeToLive = timeToLive;
        this.senderTransacted = senderTransacted;
        this.receiverCount = receiverCount;
        this.receiverTransacted = receiverTransacted;
        this.acknowledgeMode = acknowledgeMode;
        this.destinationName = destinationName;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString()
    {
    	StringBuilder sb = new StringBuilder();
        
        sb.append("Message count : ");
        sb.append(messageCount);
        sb.append("\nMessage size  : ");
        sb.append(messageSize);
        sb.append("\nQueue name    : ");
        sb.append(destinationName);
        
        sb.append("\nSender count  : ");
        sb.append(senderCount);
        sb.append("\nSender minDelay : ");
        sb.append(minDelay);
        sb.append("\nSender maxDelay : ");
        sb.append(maxDelay);
//        sb.append("\nSender deliveryMode : ");
//        sb.append(deliveryMode);
//        sb.append("\nSender priority : ");
//        sb.append(priority);
//        sb.append("\nSender timeToLive : ");
//        sb.append(timeToLive);
        sb.append("\nSender transacted : ");
        sb.append(senderTransacted);
        
        sb.append("\nReceiver count : ");
        sb.append(receiverCount);
        sb.append("\nReceiver transacted : ");
        sb.append(receiverTransacted);
        sb.append("\nReceiver acknowledgeMode : ");
        sb.append(acknowledgeMode);
        
        return sb.toString();
    }
}
