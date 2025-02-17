package by.milansky.protocol.base.packet.registry;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.packet.registry.ProtocolPacketRegistry;
import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.api.version.ProtocolVersionMapping;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author milansky
 * <p>
 * Looks lame, but this is roughly the only way to achieve constant time
 * without adding extra libraries (e.g., BidirectionalMap - de-facto the same thing)
 */
@NoArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class BasePacketRegistry implements ProtocolPacketRegistry {
    Map<Class<? extends Packet>, ProtocolVersionMapping> packetToMapping = new HashMap<>();
    Map<ProtocolVersion, Map<Integer, Class<? extends Packet>>> idToPacket = new HashMap<>();

    public void register(final @NotNull Class<? extends Packet> packetClass, final @NotNull ProtocolVersionMapping mapping) {
        packetToMapping.put(packetClass, mapping);

        mapping.asVersionMap().forEach((version, id) -> {
            idToPacket.computeIfAbsent(version, v -> new Int2ObjectArrayMap<>()).put(id, packetClass);
        });
    }

    public @Nullable ProtocolVersionMapping getMapping(final @NotNull Class<? extends Packet> packetClass) {
        return packetToMapping.get(packetClass);
    }

    public @Nullable Class<? extends Packet> getPacketById(final @NotNull ProtocolVersion version, final int id) {
        val versionMap = idToPacket.get(version);

        if (versionMap == null) return null;

        return versionMap.get(id);
    }
}
