package by.milansky.protocol.vanilla.codec;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.packet.registry.ProtocolStateRegistry;
import by.milansky.protocol.api.state.ProtocolState;
import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.vanilla.utility.ProtocolUtility;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
@Log4j2
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class VanillaPacketEncoder extends MessageToByteEncoder<Packet> {
    ProtocolVersion version;
    ProtocolStateRegistry stateRegistry;

    @Override
    protected void encode(
            final @NotNull ChannelHandlerContext channelHandlerContext,
            final @NotNull Packet packet,
            final @NotNull ByteBuf byteBuf
    ) {
        try {
            val clientboundRegistry = stateRegistry.clientbound(ProtocolState.PLAY);

            if (clientboundRegistry == null) {
                throw new IllegalStateException("Cannot find clientbound registry for packet: " + packet);
            }

            val mapping = clientboundRegistry.getMapping(packet.getClass());

            if (mapping == null) {
                throw new CorruptedFrameException("Failed to find mapper for " + packet.getClass());
            }

            ProtocolUtility.writeVarInt(byteBuf, mapping.get(version));
            packet.encode(byteBuf, version);
        } catch (final Exception e) {
            log.catching(e);
        }
    }
}
