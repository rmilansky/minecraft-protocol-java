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
public final class ClientboundSpawnEntityLiving implements Packet {
    private static final double POSITION_FACTOR = 32.0;
    private static final float ROTATION_FACTOR = 256.0F / 360.0F;
    private static final double VELOCITY_FACTOR = 8000.0;

    int entityID;
    UUID entityUUID;
    int entityTypeID;
    double positionX, positionY, positionZ;
    float yaw, pitch, headPitch;
    double velocityX, velocityY, velocityZ;

    @Override
    public void encode(final @NotNull ByteBuf byteBuf, final @NotNull ProtocolVersion version) {
        byteBuf.writeVarInt(entityID);
        if (version.lower(VanillaProtocolVersion.MINECRAFT_1_9)) {
            byteBuf.writeByte(entityTypeID & 255);
            byteBuf.writeInt((int) Math.floor(positionX * POSITION_FACTOR));
            byteBuf.writeInt((int) Math.floor(positionY * POSITION_FACTOR));
            byteBuf.writeInt((int) Math.floor(positionZ * POSITION_FACTOR));
        } else {
            byteBuf.writeUuid(entityUUID);

            if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_11)) {
                byteBuf.writeVarInt(entityTypeID);
            } else {
                byteBuf.writeByte(entityTypeID & 255);
            }

            byteBuf.writeDouble(positionX);
            byteBuf.writeDouble(positionY);
            byteBuf.writeDouble(positionZ);
        }

        byteBuf.writeByte((int) (yaw * ROTATION_FACTOR));
        byteBuf.writeByte((int) (pitch * ROTATION_FACTOR));
        byteBuf.writeByte((int) (headPitch * ROTATION_FACTOR));

        byteBuf.writeShort((int) (velocityX * VELOCITY_FACTOR));
        byteBuf.writeShort((int) (velocityY * VELOCITY_FACTOR));
        byteBuf.writeShort((int) (velocityZ * VELOCITY_FACTOR));

        if (version.lower(VanillaProtocolVersion.MINECRAFT_1_15))
            byteBuf.writeEmptyEntityMetadata(version);
    }

    @Override
    public void decode(final @NotNull ByteBuf byteBuf, final @NotNull ProtocolVersion version) {
        this.entityID = byteBuf.readVarInt();
        if (version.lower(VanillaProtocolVersion.MINECRAFT_1_9)) {
            this.entityUUID = new UUID(0L, 0L);
            this.entityTypeID = byteBuf.readByte() & 255;

            this.positionX = byteBuf.readInt() / POSITION_FACTOR;
            this.positionY = byteBuf.readInt() / POSITION_FACTOR;
            this.positionZ = byteBuf.readInt() / POSITION_FACTOR;
        } else {
            this.entityUUID = byteBuf.readUuid();
            if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_11)) {
                this.entityTypeID = byteBuf.readVarInt();
            } else {
                this.entityTypeID = byteBuf.readUnsignedByte();
            }

            this.positionX = byteBuf.readDouble();
            this.positionY = byteBuf.readDouble();
            this.positionZ = byteBuf.readDouble();
        }
        this.yaw = byteBuf.readByte() / ROTATION_FACTOR;
        this.pitch = byteBuf.readByte() / ROTATION_FACTOR;
        this.headPitch = byteBuf.readByte() / ROTATION_FACTOR;

        this.velocityX = byteBuf.readShort() / VELOCITY_FACTOR;
        this.velocityY = byteBuf.readShort() / VELOCITY_FACTOR;
        this.velocityZ = byteBuf.readShort() / VELOCITY_FACTOR;

        if (version.lower(VanillaProtocolVersion.MINECRAFT_1_15)) {
            byteBuf.skipBytes(byteBuf.readableBytes());
        }
    }
}
