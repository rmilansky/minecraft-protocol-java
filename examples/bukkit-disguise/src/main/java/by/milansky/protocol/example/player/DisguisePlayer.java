package by.milansky.protocol.example.player;

import by.milansky.protocol.bukkit.player.ProtocolPlayer;
import by.milansky.protocol.vanilla.property.Property;
import by.milansky.protocol.vanilla.standard.ClientboundUpsertPlayerInfo;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author milansky
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class DisguisePlayer {
    String name;
    @NonFinal
    String fakeName;
    @Setter
    @NonFinal
    ProtocolPlayer protocolPlayer;

    public @Nullable Player bukkitHandle() {
        return Bukkit.getPlayer(name);
    }

    public @NotNull Optional<String> fakeNameOptional() {
        return Optional.ofNullable(fakeName);
    }

    public void updateFakeName(final String fakeName) {
        if (this.fakeName != null && this.fakeName.equals(fakeName)) return;

        val oldName = this.fakeName == null ? name : this.fakeName;
        val name = fakeName == null ? this.name : fakeName;

        this.fakeName = fakeName;

        protocolPlayer.sendPacket(new ClientboundUpsertPlayerInfo(
                ClientboundUpsertPlayerInfo.Action.REMOVE_PLAYER,
                new ClientboundUpsertPlayerInfo.Item[]{
                        new ClientboundUpsertPlayerInfo.Item(
                                bukkitHandle().getUniqueId(),

                                oldName, null, null,
                                null, null, null,
                                null, null
                        )
                }
        ));

        protocolPlayer.sendPacket(new ClientboundUpsertPlayerInfo(
                ClientboundUpsertPlayerInfo.Action.ADD_PLAYER,
                new ClientboundUpsertPlayerInfo.Item[]{
                        new ClientboundUpsertPlayerInfo.Item(
                                bukkitHandle().getUniqueId(),

                                name, new Property[0], null,
                                0, 0, null,
                                null, null
                        )
                }
        ));
    }
}
