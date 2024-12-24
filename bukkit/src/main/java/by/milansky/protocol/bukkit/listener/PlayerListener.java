package by.milansky.protocol.bukkit.listener;

import by.milansky.protocol.bukkit.ProtocolBukkit;
import by.milansky.protocol.bukkit.event.ProtocolPlayerCreateEvent;
import by.milansky.protocol.bukkit.player.ProtocolPlayer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author milansky
 */
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class PlayerListener implements Listener {
    ProtocolBukkit protocolBukkit;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        val protocolPlayer = ProtocolPlayer.create(event.getPlayer());

        protocolPlayer.appendPipeline();

        protocolBukkit.getServer().getPluginManager().callEvent(ProtocolPlayerCreateEvent.create(protocolPlayer));
    }
}
