package net.timewalker.ffmq3.transport.packet.response;

import net.timewalker.ffmq3.transport.packet.AbstractResponsePacket;
import net.timewalker.ffmq3.transport.packet.PacketType;

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
     * @see net.timewalker.ffmq3.network.packet.AbstractPacket#getType()
     */
    public byte getType()
    {
        return PacketType.R_ROLLBACK_MESSAGE;
    }
}
