package by.milansky.protocol.vanilla.standard;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.vanilla.property.Property;
import by.milansky.protocol.vanilla.utility.ProtocolUtility;
import by.milansky.protocol.vanilla.version.VanillaProtocolVersion;
import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
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
public final class ClientboundUpsertPlayerInfo implements Packet {
    Action action;
    Item[] items;

    @Override
    public void encode(final @NotNull ByteBuf byteBuf, final @NotNull ProtocolVersion version) {
        byteBuf.writeVarInt(action.ordinal());
        byteBuf.writeVarInt(items.length);

        for (val item : items) {
            byteBuf.writeUuid(item.uuid());

            switch (action) {
                case ADD_PLAYER:
                    byteBuf.writeString(item.username);
                    byteBuf.writeProperties(item.properties);
                    byteBuf.writeVarInt(item.gamemode);
                    byteBuf.writeVarInt(item.ping);

                    byteBuf.writeBoolean(item.displayName != null);
                    if (item.displayName != null) byteBuf.writeComponent(version, item.displayName, false);

                    // No chat key
                    if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_19)) byteBuf.writeBoolean(false);
                    break;
                case UPDATE_GAMEMODE:
                    byteBuf.writeVarInt(item.gamemode);
                    break;
                case UPDATE_LATENCY:
                    byteBuf.writeVarInt(item.ping);
                    break;
                case UPDATE_DISPLAY_NAME:
                    byteBuf.writeBoolean(item.displayName != null);
                    if (item.displayName != null) byteBuf.writeComponent(version, item.displayName, false);

                    break;
                case REMOVE_PLAYER:
                    break;
            }
        }
    }

    @Override
    public void decode(final @NotNull ByteBuf byteBuf, final @NotNull ProtocolVersion version) {
        action = Action.values()[byteBuf.readVarInt()];
        items = new Item[byteBuf.readVarInt()];

        for (int i = 0; i < items.length; i++) {
            val item = items[i] = new Item();
            item.uuid(byteBuf.readUuid());

            switch (action) {
                case ADD_PLAYER:
                    item.username = byteBuf.readString();
                    item.properties = byteBuf.readProperties();
                    item.gamemode = byteBuf.readVarInt();
                    item.ping = byteBuf.readVarInt();

                    if (byteBuf.readBoolean()) item.displayName = byteBuf.readComponent(version, false);
                    if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_19)) {
                        if (byteBuf.readBoolean()) {
                            byteBuf.skipBytes(byteBuf.readableBytes());
                            throw new IllegalStateException();
                        }
                    }
                    break;
                case UPDATE_GAMEMODE:
                    item.gamemode = byteBuf.readVarInt();
                    break;
                case UPDATE_LATENCY:
                    item.ping = byteBuf.readVarInt();
                    break;
                case UPDATE_DISPLAY_NAME:
                    if (byteBuf.readBoolean()) item.displayName = byteBuf.readComponent(version, false);
            }
        }
    }

    public enum Action {
        ADD_PLAYER,
        UPDATE_GAMEMODE,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME,
        REMOVE_PLAYER;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        UUID uuid;

        String username;
        Property[] properties;

        Boolean listed;

        Integer gamemode;
        Integer ping;

        Component displayName;

        Integer listOrder;

        Boolean showHat;
    }
}
