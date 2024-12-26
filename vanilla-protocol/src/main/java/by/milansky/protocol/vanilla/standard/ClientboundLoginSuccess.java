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

import java.util.UUID;

/**
 * @author milansky
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
@ExtensionMethod({ProtocolUtility.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ClientboundLoginSuccess implements Packet {
    UUID uuid;
    String name;

    @Override
    public void encode(final @NotNull ByteBuf byteBuf, final @NotNull ProtocolVersion version) {
        if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_19)) {
            byteBuf.writeUuid(uuid);
        } else if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_16)) {
            byteBuf.writeUuidIntArray(uuid);
        } else if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_7_6)) {
            byteBuf.writeString(uuid.toString());
        }

        byteBuf.writeString(name);
    }

    @Override
    public void decode(final @NotNull ByteBuf byteBuf, final @NotNull ProtocolVersion version) {
        if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_19)) {
            uuid = byteBuf.readUuid();
        } else if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_16)) {
            uuid = byteBuf.readUuidIntArray();
        } else if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_7_6)) {
            uuid = UUID.fromString(byteBuf.readString(36));
        }

        name = byteBuf.readString(16);
        byteBuf.skipBytes(byteBuf.readableBytes());
    }
}
