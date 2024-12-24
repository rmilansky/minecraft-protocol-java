package by.milansky.protocol.base.packet.handler;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.packet.handler.PacketHandleResult;
import by.milansky.protocol.api.packet.handler.PacketHandler;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
@NoArgsConstructor(staticName = "create")
public final class BaseEmptyPacketHandler implements PacketHandler {
    @Override
    public @NotNull PacketHandleResult handle(final @NotNull Packet packet) {
        return BasePacketHandleResult.ok();
    }
}
