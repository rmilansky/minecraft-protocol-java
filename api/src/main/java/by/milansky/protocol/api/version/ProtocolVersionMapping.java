package by.milansky.protocol.api.version;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

/**
 * @author milansky
 */
public interface ProtocolVersionMapping {
    int get(@NotNull ProtocolVersion version);

    @NotNull
    @Unmodifiable
    Map<ProtocolVersion, Integer> asVersionMap();
}
