package by.milansky.protocol.vanilla.standard;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.vanilla.utility.ProtocolUtility;
import by.milansky.protocol.vanilla.version.VanillaProtocolVersion;
import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author milansky
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
@ExtensionMethod({ProtocolUtility.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ClientboundTeam implements Packet {
    String name;
    TeamMode mode;
    Component displayName, prefix, suffix;
    NameTagVisibility nameTagVisibility;
    CollisionRule collisionRule;
    int color;
    byte friendlyFire;
    String[] players;

    @Override
    public void encode(@NotNull ByteBuf byteBuf, @NotNull ProtocolVersion version) {
        byteBuf.writeString(name);
        byteBuf.writeByte(mode.ordinal());

        if (mode == TeamMode.CREATE || mode == TeamMode.UPDATE) {
            byteBuf.writeComponent(version, displayName, version.lower(VanillaProtocolVersion.MINECRAFT_1_13));

            if (version.lower(VanillaProtocolVersion.MINECRAFT_1_13)) {
                byteBuf.writeComponent(version, prefix, true);
                byteBuf.writeComponent(version, suffix, true);
            }

            byteBuf.writeByte(friendlyFire);
            byteBuf.writeString(nameTagVisibility.identifier());

            if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_9))
                byteBuf.writeString(collisionRule.identifier());

            if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_13)) {
                byteBuf.writeInt(color);
            } else {
                byteBuf.writeByte(color);
            }

            if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_13)) {
                byteBuf.writeComponent(version, prefix, false);
                byteBuf.writeComponent(version, suffix, false);
            }
        }

        if (mode == TeamMode.CREATE || mode == TeamMode.ADD_PLAYER || mode == TeamMode.REMOVE_PLAYER)
            byteBuf.writeStringArray(players);
    }

    @Override
    public void decode(@NotNull ByteBuf byteBuf, @NotNull ProtocolVersion version) {
        name = byteBuf.readString();
        mode = TeamMode.VALUES[byteBuf.readByte()];

        if (mode == TeamMode.CREATE || mode == TeamMode.UPDATE) {
            displayName = byteBuf.readComponent(version, version.lower(VanillaProtocolVersion.MINECRAFT_1_13));

            if (version.lower(VanillaProtocolVersion.MINECRAFT_1_13)) {
                prefix = byteBuf.readComponent(version, true);
                suffix = byteBuf.readComponent(version, true);
            }

            friendlyFire = byteBuf.readByte();
            nameTagVisibility = NameTagVisibility.visibilityByIdentifier(byteBuf.readString());

            if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_9))
                collisionRule = CollisionRule.ruleByIdentifier(byteBuf.readString());
            color = version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_13) ? byteBuf.readVarInt() : byteBuf.readByte();

            if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_13)) {
                prefix = byteBuf.readComponent(version, false);
                suffix = byteBuf.readComponent(version, false);
            }
        }

        if (mode == TeamMode.CREATE || mode == TeamMode.ADD_PLAYER || mode == TeamMode.REMOVE_PLAYER)
            players = byteBuf.readStringArray();
    }

    public boolean containsPlayer(final String targetName) {
        for (val playerName : players)
            if (playerName.equals(targetName))
                return true;

        return false;
    }

    public enum TeamMode {
        CREATE, REMOVE, UPDATE, ADD_PLAYER, REMOVE_PLAYER;

        public static final TeamMode[] VALUES = values();
    }

    @Getter
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public enum NameTagVisibility {
        ALWAYS("always"),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam"),
        NEVER("never");

        private static final Map<String, NameTagVisibility> NAME_TAG_VISIBILITIES = new HashMap<>();

        static {
            for (val visibility : NameTagVisibility.values())
                NAME_TAG_VISIBILITIES.put(visibility.identifier(), visibility);
        }

        String identifier;

        public static @Nullable NameTagVisibility visibilityByIdentifier(final @NotNull String identifier) {
            return NAME_TAG_VISIBILITIES.get(identifier);
        }
    }

    @Getter
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public enum CollisionRule {
        ALWAYS("always"),
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        PUSH_OWN_TEAM("pushOwnTeam"),
        NEVER("never");

        private static final Map<String, CollisionRule> COLLISION_RULES = new HashMap<>();

        static {
            for (val rule : CollisionRule.values())
                COLLISION_RULES.put(rule.identifier(), rule);
        }

        String identifier;

        public static @Nullable CollisionRule ruleByIdentifier(final @NotNull String name) {
            return COLLISION_RULES.get(name);
        }
    }
}
