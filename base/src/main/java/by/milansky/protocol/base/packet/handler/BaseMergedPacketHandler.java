package by.milansky.protocol.base.packet.handler;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.packet.handler.PacketHandleResult;
import by.milansky.protocol.api.packet.handler.PacketHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author milansky
 */
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class BaseMergedPacketHandler implements PacketHandler {
    Collection<PacketHandler> childHandlers;

    private static Runnable mergedRunnables(final Runnable... runnables) {
        return () -> {
            for (val runnable : runnables) {
                if (runnable == null) continue;

                runnable.run();
            }
        };
    }

    public static BaseMergedPacketHandler create(final PacketHandler... childHandlers) {
        // TODO: optimize algorithm if one of the handlers is already a merged handler
        return create(new HashSet<>()).append(childHandlers);
    }

    @Override
    public @NotNull PacketHandleResult handle(final @NotNull Packet packet) {
        val result = BasePacketHandleResult.createEmpty();

        for (val childHandler : childHandlers) {
            val childResult = childHandler.handle(packet);

            if (childResult.replacement() != null)
                result.setReplacement(childResult.replacement());

            if (childResult.cancelled())
                result.setCancelled(true);

            result.setAfterWrite(mergedRunnables(result.getAfterWrite(), childResult::afterWrite));
        }

        return result;
    }

    public @NotNull BaseMergedPacketHandler append(final PacketHandler... packetHandlers) {
        childHandlers.addAll(List.of(packetHandlers));

        return this;
    }
}
