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
public final class ServerboundTabcomplete implements Packet {
    int transactionId;
    String cursor;
    boolean assumeCommand, hasPosition;
    long position;

    @Override
    public void encode(final @NotNull ByteBuf byteBuf, final @NotNull ProtocolVersion version) {
        if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_13)) byteBuf.writeVarInt(transactionId);

        byteBuf.writeString(cursor);

        if (version.lower(VanillaProtocolVersion.MINECRAFT_1_13)) {
            if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_9)) {
                byteBuf.writeBoolean(assumeCommand);
            }

            byteBuf.writeBoolean(hasPosition);

            if (hasPosition) byteBuf.writeLong(position);
        }
    }

    @Override
    public void decode(final @NotNull ByteBuf byteBuf, final @NotNull ProtocolVersion version) {
        if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_13)) {
            transactionId = byteBuf.readVarInt();
        }
        cursor = byteBuf.readString();

        if (version.lower(VanillaProtocolVersion.MINECRAFT_1_13)) {
            if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_9)) {
                assumeCommand = byteBuf.readBoolean();
            }

            if (hasPosition = byteBuf.readBoolean()) {
                position = byteBuf.readLong();
            }
        }
    }
}
