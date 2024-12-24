package by.milansky.protocol.api.version;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author milansky
 */
public interface ProtocolVersion {
    int protocol();

    @Nullable ProtocolVersion next();

    default boolean greater(@NotNull ProtocolVersion other) {
        return protocol() > other.protocol();
    }

    default boolean greaterEqual(@NotNull ProtocolVersion other) {
        return protocol() >= other.protocol();
    }

    default boolean lower(@NotNull ProtocolVersion other) {
        return protocol() < other.protocol();
    }

    default boolean lowerEqual(@NotNull ProtocolVersion other) {
        return protocol() <= other.protocol();
    }
}
