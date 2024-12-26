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
    Optional<String> fakeName = Optional.empty();
    @Setter
    @NonFinal
    ProtocolPlayer protocolPlayer;

    public @Nullable Player bukkitHandle() {
        return Bukkit.getPlayer(name);
    }

    public void updateFakeName(final String fakeName) {
        updateFakeName(Optional.ofNullable(fakeName));
    }

    public void updateFakeName(final Optional<String> fakeName) {
        val oldName = this.fakeName.orElse(this.name);
        val name = fakeName.orElse(this.name);

        this.fakeName = fakeName;

        if (oldName.equals(name)) return;

        protocolPlayer.sendPacket(new ClientboundUpsertPlayerInfo(
                ClientboundUpsertPlayerInfo.Action.REMOVE_PLAYER,
                new ClientboundUpsertPlayerInfo.Item[]{
                        new ClientboundUpsertPlayerInfo.Item(
                                bukkitHandle().getUniqueId(),

                                oldName,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                }
        ));

        protocolPlayer.sendPacket(new ClientboundUpsertPlayerInfo(
                ClientboundUpsertPlayerInfo.Action.ADD_PLAYER,
                new ClientboundUpsertPlayerInfo.Item[]{
                        new ClientboundUpsertPlayerInfo.Item(
                                bukkitHandle().getUniqueId(),

                                name,
                                new Property[0],
                                null,
                                0,
                                0,
                                null,
                                null,
                                null
                        )
                }
        ));
    }
}
