package by.milansky.protocol.api.packet.registry;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.api.version.ProtocolVersionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author milansky
 */
public interface ProtocolPacketRegistry {
    void register(@NotNull Class<? extends Packet> packetClass, @NotNull ProtocolVersionMapping mapping);

    @Nullable Class<? extends Packet> getPacketById(@NotNull ProtocolVersion version, int id);

    @Nullable ProtocolVersionMapping getMapping(final @NotNull Class<? extends Packet> packetClass);
}
