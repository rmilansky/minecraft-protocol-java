package by.milansky.protocol.base.version;

import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.api.version.ProtocolVersionMapping;
import by.milansky.protocol.base.throwable.UnsupportedMappingException;
import by.milansky.protocol.base.throwable.UnsupportedPacketException;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * @author milansky
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class UnmodifiableVersionMapping implements ProtocolVersionMapping {
    @Unmodifiable
    Map<ProtocolVersion, Integer> downstreamMap;
    @Nullable ProtocolVersion latestVersion;

    public int get(final @NotNull ProtocolVersion version) {
        if (latestVersion != null && version.greaterEqual(latestVersion)) {
            throw new UnsupportedPacketException();
        }

        return downstreamMap.getOrDefault(version, -1);
    }

    public @NotNull @Unmodifiable Map<ProtocolVersion, Integer> asVersionMap() {
        return downstreamMap;
    }

    @Override
    public String toString() {
        return downstreamMap.toString();
    }

    public static @NotNull UnmodifiableVersionMapping createMapping(@NotNull Object... any) {
        @Nullable ProtocolVersion latestVersion = null;

        if (any.length % 2 != 0) {
            latestVersion = (ProtocolVersion) any[any.length - 1];

            any = Arrays.copyOfRange(any, 0, any.length - 1);
        }

        val downstreamMap = new Object2IntArrayMap<ProtocolVersion>();

        populateDownstreamMap(any, downstreamMap);
        fillGapsInMapping(latestVersion, downstreamMap);

        return new UnmodifiableVersionMapping(Collections.unmodifiableMap(downstreamMap), latestVersion);
    }

    public static @NotNull UnmodifiableVersionMapping fromMap(final @NotNull Map<ProtocolVersion, Integer> downstreamMap) {
        val completeMap = new Object2IntArrayMap<>(downstreamMap);

        fillGapsInMapping(null, completeMap);

        return new UnmodifiableVersionMapping(Collections.unmodifiableMap(completeMap), null);
    }

    private static void populateDownstreamMap(
            final @NotNull Object[] any,
            final @NotNull Object2IntMap<ProtocolVersion> downstreamMap
    ) {
        for (int i = 0; i < any.length; i += 2) {
            validateMappingPair(any, i);

            val version = (ProtocolVersion) any[i];
            val identifier = (int) any[i + 1];

            downstreamMap.put(version, identifier);

            if (i + 2 < any.length) {
                val nextVersion = (ProtocolVersion) any[i + 2];

                for (var protocol = version; protocol != null && protocol.lower(nextVersion); protocol = protocol.next()) {
                    downstreamMap.put(protocol, identifier);
                }
            }
        }
    }

    private static void validateMappingPair(final @NotNull Object[] any, final int index) {
        if (!(any[index] instanceof ProtocolVersion)) {
            throw new UnsupportedMappingException("any[2n] must be an instance of ProtocolVersion");
        }

        if (!(any[index + 1] instanceof Integer)) {
            throw new UnsupportedMappingException("any[2n + 1] must be an instance of Integer");
        }
    }

    private static void fillGapsInMapping(ProtocolVersion latestVersion, Object2IntArrayMap<ProtocolVersion> downstreamMap) {
        ProtocolVersion highestVersion = null;
        int highestIdentifier = -1;

        for (val entry : downstreamMap.entrySet()) {
            val version = entry.getKey();
            val identifier = (int) entry.getValue();

            if (highestVersion == null || version.greater(highestVersion)) {
                highestVersion = version;
                highestIdentifier = identifier;
            }

            var nextVersion = version.next();
            while (nextVersion != null && !downstreamMap.containsKey(nextVersion)) {
                downstreamMap.put(nextVersion, identifier);
                nextVersion = nextVersion.next();
            }
        }

        if (latestVersion == null && highestVersion != null) {
            var current = highestVersion.next();
            while (current != null) {
                downstreamMap.put(current, highestIdentifier);
                current = current.next();
            }
        }
    }
}
