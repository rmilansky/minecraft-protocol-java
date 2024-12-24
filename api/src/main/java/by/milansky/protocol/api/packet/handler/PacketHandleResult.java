package by.milansky.protocol.api.packet.handler;

import by.milansky.protocol.api.packet.Packet;
import org.jetbrains.annotations.Nullable;

/**
 * @author milansky
 */
public interface PacketHandleResult {
    @Nullable Packet replacement();

    boolean cancelled();

    void afterWrite();
}
