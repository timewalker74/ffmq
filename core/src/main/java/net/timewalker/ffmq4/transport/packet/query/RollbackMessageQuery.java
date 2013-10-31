package net.timewalker.ffmq3.transport.packet.query;

import net.timewalker.ffmq3.transport.packet.PacketType;
import net.timewalker.ffmq3.utils.RawDataBuffer;

/**
 * RollbackMessagesQuery
 */
public final class RollbackMessageQuery extends AbstractConsumerQuery
{
    private String messageId;
    
    /**
	 * @param messageId the messageId to set
	 */
	public void setMessageId(String messageId)
	{
		this.messageId = messageId;
	}
    
    /**
	 * @return the messageId
	 */
	public String getMessageId()
	{
		return messageId;
	}
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#getType()
     */
    @Override
	public byte getType()
    {
        return PacketType.Q_ROLLBACK_MESSAGE;
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#serializeTo(net.timewalker.ffmq3.utils.RawDataOutputStream)
     */
    @Override
	protected void serializeTo(RawDataBuffer out)
    {
        super.serializeTo(out);
        out.writeUTF(messageId);
    }

    /* (non-Javadoc)
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#unserializeFrom(net.timewalker.ffmq3.utils.RawDataInputStream)
     */
    @Override
	protected void unserializeFrom(RawDataBuffer in)
    {
        super.unserializeFrom(in);
        messageId = in.readUTF();
    }
}
