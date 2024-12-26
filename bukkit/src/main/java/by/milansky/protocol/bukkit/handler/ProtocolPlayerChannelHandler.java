package by.milansky.protocol.bukkit.handler;

import by.milansky.protocol.api.packet.handler.PacketHandleResult;
import by.milansky.protocol.base.packet.handler.BasePacketHandleResult;
import by.milansky.protocol.base.packet.handler.annotation.PacketProcessor;
import by.milansky.protocol.vanilla.standard.ClientboundLoginSuccess;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author milansky
 */
@Getter
@Log4j2
@Accessors(fluent = true)
@NoArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ProtocolPlayerChannelHandler {
    Map<String, Channel> receivedChannels = new HashMap<>();

    @PacketProcessor
    public PacketHandleResult handle(final @NotNull Channel channel, final @NotNull ClientboundLoginSuccess login) {
        receivedChannels.put(login.name(), channel);

        return BasePacketHandleResult.ok();
    }
}
