package net.timewalker.ffmq4.transport.packet.response;

import net.timewalker.ffmq4.transport.packet.AbstractResponsePacket;
import net.timewalker.ffmq4.transport.packet.PacketType;

/**
 * RollbackMessagesResponse
 */
public final class RollbackMessageResponse extends AbstractResponsePacket
{
    /**
     * Constructor
     */
    public RollbackMessageResponse()
    {
        super();
    }
    
    /* (non-Javadoc)
     * @see net.timewalker.ffmq4.network.packet.AbstractPacket#getType()
     */
    @Override
	public byte getType()
    {
        return PacketType.R_ROLLBACK_MESSAGE;
    }
}
