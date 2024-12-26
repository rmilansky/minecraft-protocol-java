package by.milansky.protocol.example.handler;

import by.milansky.protocol.api.packet.handler.PacketHandleResult;
import by.milansky.protocol.base.packet.handler.BasePacketHandleResult;
import by.milansky.protocol.base.packet.handler.annotation.PacketProcessor;
import by.milansky.protocol.vanilla.standard.ServerboundTabcomplete;
import io.netty.channel.Channel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
@Log4j2
@NoArgsConstructor(staticName = "create")
public final class NametagDebugHandler {
    @PacketProcessor
    public @NotNull PacketHandleResult handle(final Channel channel, final ServerboundTabcomplete tabcomplete) {
        log.info("Inbound tabcomplete packet: {}", tabcomplete);

        return BasePacketHandleResult.cancel();
    }
}
