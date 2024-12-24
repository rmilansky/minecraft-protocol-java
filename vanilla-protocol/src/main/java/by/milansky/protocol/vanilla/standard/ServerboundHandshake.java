package by.milansky.protocol.vanilla.standard;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.vanilla.utility.ProtocolUtility;
import by.milansky.protocol.vanilla.version.VanillaProtocolVersion;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
@ExtensionMethod({ProtocolUtility.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ServerboundHandshake implements Packet {
    ProtocolVersion protocolVersion;
    String host;
    int port, nextState;

    @Override
    public void encode(final @NotNull ByteBuf byteBuf, final @NotNull ProtocolVersion version) {
        byteBuf.writeVarInt(protocolVersion.protocol());
        byteBuf.writeString(host);
        byteBuf.writeShort(port);
        byteBuf.writeVarInt(nextState);
    }

    @Override
    public void decode(final @NotNull ByteBuf byteBuf, final @NotNull ProtocolVersion version) {
        protocolVersion = VanillaProtocolVersion.versionByProtocol(byteBuf.readVarInt());
        host = byteBuf.readString();
        port = byteBuf.readUnsignedShort();
        nextState = byteBuf.readVarInt();
    }
}
