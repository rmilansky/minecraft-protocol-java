package by.milansky.protocol.bukkit.player;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.packet.handler.PacketHandler;
import by.milansky.protocol.base.packet.handler.BaseMergedPacketHandler;
import by.milansky.protocol.vanilla.utility.ChannelUtility;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
@Log4j2
@Getter
@Accessors(fluent = true)
@ExtensionMethod({ChannelUtility.class})
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ProtocolPlayer {
    Player nativePlayer;
    Channel channel;

    public void appendPacketHandler(final @NotNull PacketHandler packetHandler) {
        val savedPacketHandler = channel.packetHandler();

        if (savedPacketHandler instanceof BaseMergedPacketHandler mergedHandler) {
            mergedHandler.append(packetHandler);
            return;
        }

        channel.updatePacketHandler(BaseMergedPacketHandler.create(savedPacketHandler, packetHandler));
    }

    public void sendPacket(final @NotNull Packet packet) {
        channel.writeAndFlush(packet);
    }
}
