package by.milansky.protocol.example.listener;

import by.milansky.protocol.base.packet.handler.annotation.AnnotationBasedHandler;
import by.milansky.protocol.bukkit.event.ProtocolPlayerCreateEvent;
import by.milansky.protocol.example.handler.DisguisePacketHandler;
import by.milansky.protocol.example.player.DisguisePlayerStorage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * @author milansky
 */
@Log4j2
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class DisguiseListener implements Listener {
    DisguisePlayerStorage storage;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCreate(final ProtocolPlayerCreateEvent event) {
        val protocolPlayer = event.protocolPlayer();

        protocolPlayer.appendPacketHandler(AnnotationBasedHandler.create(DisguisePacketHandler.create(storage)));

        storage.disguisePlayer(protocolPlayer.nativePlayer()).protocolPlayer(protocolPlayer);
    }
}
