package by.milansky.protocol.base.packet.registry;

import by.milansky.protocol.api.packet.registry.ProtocolPacketRegistry;
import lombok.AccessLevel;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author milansky
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class BaseSuppliedPacketRegistry implements ProtocolPacketRegistry {
    @Delegate
    ProtocolPacketRegistry parentRegistry;

    private BaseSuppliedPacketRegistry(
            final @NotNull ProtocolPacketRegistry parentRegistry,
            final @NotNull Consumer<ProtocolPacketRegistry> registryConsumer
    ) {
        this.parentRegistry = parentRegistry;

        registryConsumer.accept(parentRegistry);
    }

    private BaseSuppliedPacketRegistry(final @NotNull Consumer<ProtocolPacketRegistry> registryConsumer) {
        this(BasePacketRegistry.create(), registryConsumer);
    }

    @Contract("_ -> new")
    public static ProtocolPacketRegistry create(final @NotNull Consumer<ProtocolPacketRegistry> registryConsumer) {
        return new BaseSuppliedPacketRegistry(BasePacketRegistry.create(), registryConsumer);
    }
}
