package by.milansky.protocol.vanilla.utility;

import by.milansky.protocol.api.packet.handler.PacketHandler;
import by.milansky.protocol.api.state.ProtocolState;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
@Log4j2
@UtilityClass
public final class ChannelUtility {
    private static final AttributeKey<ProtocolState> PROTOCOL_STATE_KEY = AttributeKey.valueOf("protocolState");
    private static final AttributeKey<PacketHandler> PACKET_HANDLER_KEY = AttributeKey.valueOf("packetHandler");

    public @NotNull ProtocolState protocolState(final @NotNull Channel channel) {
        return channel.attr(PROTOCOL_STATE_KEY).get();
    }

    public void updateProtocolState(final @NotNull Channel channel, final @NotNull ProtocolState state) {
        channel.attr(PROTOCOL_STATE_KEY).set(state);
    }

    public @NotNull PacketHandler packetHandler(final @NotNull Channel channel) {
        return channel.attr(PACKET_HANDLER_KEY).get();
    }

    public void updatePacketHandler(final @NotNull Channel channel, final @NotNull PacketHandler handler) {
        channel.attr(PACKET_HANDLER_KEY).set(handler);
    }
}
