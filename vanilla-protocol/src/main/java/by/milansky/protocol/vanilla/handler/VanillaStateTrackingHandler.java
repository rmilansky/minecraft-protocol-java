package by.milansky.protocol.vanilla.handler;

import by.milansky.protocol.api.packet.handler.PacketHandleResult;
import by.milansky.protocol.api.state.ProtocolState;
import by.milansky.protocol.base.packet.handler.BasePacketHandleResult;
import by.milansky.protocol.base.packet.handler.annotation.PacketProcessor;
import by.milansky.protocol.vanilla.standard.ClientboundLoginSuccess;
import by.milansky.protocol.vanilla.standard.ServerboundHandshake;
import by.milansky.protocol.vanilla.utility.ChannelUtility;
import io.netty.channel.Channel;
import lombok.NoArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
@Log4j2
@ExtensionMethod({ChannelUtility.class})
@NoArgsConstructor(staticName = "create")
public final class VanillaStateTrackingHandler {
    @PacketProcessor
    public @NotNull PacketHandleResult handle(final Channel channel, final @NotNull ServerboundHandshake handshake) {
        channel.updateProtocolState(switch (handshake.nextState()) {
            case 1 -> ProtocolState.STATUS;
            case 2 -> ProtocolState.LOGIN;

            default -> throw new IllegalStateException("Unexpected value: " + handshake.nextState());
        });

        return BasePacketHandleResult.ok();
    }

    @PacketProcessor
    public @NotNull PacketHandleResult handle(final Channel channel, final @NotNull ClientboundLoginSuccess success) {
        // TODO: Support for configuration stage to avoid i/o errors in configuration stage
        channel.updateProtocolState(ProtocolState.PLAY);

        return BasePacketHandleResult.ok();
    }
}
