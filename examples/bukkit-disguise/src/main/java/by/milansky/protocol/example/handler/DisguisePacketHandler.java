package by.milansky.protocol.example.handler;

import by.milansky.protocol.api.packet.handler.PacketHandleResult;
import by.milansky.protocol.base.packet.handler.BasePacketHandleResult;
import by.milansky.protocol.base.packet.handler.annotation.PacketProcessor;
import by.milansky.protocol.example.player.DisguisePlayerStorage;
import by.milansky.protocol.vanilla.standard.ClientboundTeam;
import by.milansky.protocol.vanilla.standard.ClientboundUpsertPlayerInfo;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
@Log4j2
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class DisguisePacketHandler {
    DisguisePlayerStorage storage;

    @PacketProcessor
    public @NotNull PacketHandleResult handle(final Channel ignored, final ClientboundUpsertPlayerInfo playerInfo) {
        for (val item : playerInfo.items()) {
            val player = storage.disguisePlayer(item.username());

            player.fakeNameOptional().ifPresent(item::username);
        }

        return BasePacketHandleResult.replace(playerInfo);
    }

    @PacketProcessor
    public @NotNull PacketHandleResult handle(final Channel ignored, final ClientboundTeam team) {
        val players = team.players();

        if (players == null) return BasePacketHandleResult.ok();

        for (int i = 0; i < players.length; i++) {
            val teamPlayerName = players[i];
            val disguisePlayer = storage.disguisePlayer(teamPlayerName);

            players[i] = disguisePlayer.fakeNameOptional().orElse(teamPlayerName);
        }

        return BasePacketHandleResult.replace(team);
    }
}
