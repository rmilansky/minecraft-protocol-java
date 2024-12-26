package by.milansky.protocol.bukkit.listener;

import by.milansky.protocol.bukkit.event.ProtocolPlayerCreateEvent;
import by.milansky.protocol.bukkit.handler.ProtocolPlayerChannelHandler;
import by.milansky.protocol.bukkit.player.ProtocolPlayer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author milansky
 */
@Log4j2
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ProtocolPlayerListener implements Listener {
    ProtocolPlayerChannelHandler protocolPlayerChannelHandler;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        val player = event.getPlayer();
        val protocolPlayer = ProtocolPlayer.create(player,
                protocolPlayerChannelHandler.receivedChannels().remove(player.getName()));

        Bukkit.getServer().getPluginManager().callEvent(ProtocolPlayerCreateEvent.create(protocolPlayer));
    }
}
