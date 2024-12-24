package by.milansky.protocol.base.packet.registry;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.packet.registry.ProtocolPacketRegistry;
import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.api.version.ProtocolVersionMapping;
import io.netty.util.collection.IntObjectHashMap;
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
 *
 * Выглядит убого, но примерно только так можно добиться константного времени
 * не подключая лишних либ (e.g. BidirectionalMap - де-факто то же самое)
 */
@NoArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class BasePacketRegistry implements ProtocolPacketRegistry {
    Map<Class<? extends Packet>, ProtocolVersionMapping> packetToMapping = new HashMap<>();
    Map<ProtocolVersion, Map<Integer, Class<? extends Packet>>> idToPacket = new HashMap<>();

    public void register(final @NotNull Class<? extends Packet> packetClass, final @NotNull ProtocolVersionMapping mapping) {
        packetToMapping.put(packetClass, mapping);

        mapping.asVersionMap().forEach((version, id) -> {
            idToPacket.computeIfAbsent(version, v -> new IntObjectHashMap<>()).put(id, packetClass);
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
