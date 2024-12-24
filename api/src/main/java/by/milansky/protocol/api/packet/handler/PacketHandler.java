package by.milansky.protocol.api.packet.handler;

import by.milansky.protocol.api.packet.Packet;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
public interface PacketHandler {
    @NotNull PacketHandleResult handle(@NotNull Packet packet);
}
