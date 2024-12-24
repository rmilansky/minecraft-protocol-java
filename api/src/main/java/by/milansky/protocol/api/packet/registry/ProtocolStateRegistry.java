package by.milansky.protocol.api.packet.registry;

import by.milansky.protocol.api.state.ProtocolState;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
public interface ProtocolStateRegistry {
    ProtocolPacketRegistry clientbound(@NotNull ProtocolState state);

    ProtocolPacketRegistry serverbound(@NotNull ProtocolState state);
}
