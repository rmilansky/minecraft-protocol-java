package by.milansky.protocol.api.packet;

import by.milansky.protocol.api.version.ProtocolVersion;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
public interface Packet {
    void encode(@NotNull ByteBuf byteBuf, @NotNull ProtocolVersion version);

    void decode(@NotNull ByteBuf byteBuf, @NotNull ProtocolVersion version);
}
