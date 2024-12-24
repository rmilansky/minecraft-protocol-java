package by.milansky.protocol.base.packet.handler;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.packet.handler.PacketHandleResult;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author milansky
 */
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "create")
@NoArgsConstructor(staticName = "createEmpty")
public final class BasePacketHandleResult implements PacketHandleResult {
    private static final Runnable EMPTY_NOT_NULL_RUNNABLE = () -> {};

    Runnable afterWrite;
    @Nullable
    Packet replacement;
    boolean cancelled;

    public static @NotNull PacketHandleResult cancel() {
        return create(EMPTY_NOT_NULL_RUNNABLE, null, true);
    }

    public static @NotNull PacketHandleResult replace(final @NotNull Packet replacement) {
        return create(EMPTY_NOT_NULL_RUNNABLE, replacement, false);
    }

    public static @NotNull PacketHandleResult replace(final @NotNull Packet replacement, final @NotNull Runnable afterWrite) {
        return create(afterWrite, replacement, false);
    }

    public static @NotNull PacketHandleResult ok() {
        return create(EMPTY_NOT_NULL_RUNNABLE, null, false);
    }

    @Override
    public @Nullable Packet replacement() {
        return replacement;
    }

    @Override
    public boolean cancelled() {
        return cancelled;
    }

    @Override
    public void afterWrite() {
        afterWrite.run();
    }
}
