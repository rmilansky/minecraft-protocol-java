package by.milansky.protocol.base.packet.registry;

import by.milansky.protocol.api.packet.registry.ProtocolPacketRegistry;
import by.milansky.protocol.api.packet.registry.ProtocolStateRegistry;
import by.milansky.protocol.api.state.ProtocolState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author milansky
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class BaseStateRegistry implements ProtocolStateRegistry {
    Map<ProtocolState, ProtocolPacketRegistry> clientbound, serverbound;

    public BaseStateRegistry() {
        this(new EnumMap<>(ProtocolState.class), new EnumMap<>(ProtocolState.class));
    }

    @Override
    public final ProtocolPacketRegistry clientbound(final @NotNull ProtocolState state) {
        return clientbound.get(state);
    }

    @Override
    public final ProtocolPacketRegistry serverbound(final @NotNull ProtocolState state) {
        return serverbound.get(state);
    }

    protected final void registerClientboundState(
            final @NotNull ProtocolState state,
            final @NotNull ProtocolPacketRegistry registry
    ) {
        clientbound.put(state, registry);
    }

    protected final void registerServerboundState(
            final @NotNull ProtocolState state,
            final @NotNull ProtocolPacketRegistry registry
    ) {
        serverbound.put(state, registry);
    }
}
