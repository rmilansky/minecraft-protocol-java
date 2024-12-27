package by.milansky.protocol.vanilla.standard;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.vanilla.utility.ProtocolUtility;
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
public final class ClientboundUpdateHealth implements Packet {
    float health, foodSaturation;
    int food;

    @Override
    public void encode(final @NotNull ByteBuf byteBuf, final @NotNull ProtocolVersion version) {
        byteBuf.writeFloat(health);
        byteBuf.writeVarInt(food);
        byteBuf.writeFloat(foodSaturation);
    }

    @Override
    public void decode(final @NotNull ByteBuf byteBuf, final @NotNull ProtocolVersion version) {
        health = byteBuf.readFloat();
        food = byteBuf.readVarInt();
        foodSaturation = byteBuf.readFloat();
    }
}
