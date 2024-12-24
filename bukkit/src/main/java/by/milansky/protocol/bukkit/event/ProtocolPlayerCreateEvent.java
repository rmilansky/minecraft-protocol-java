package by.milansky.protocol.bukkit.event;

import by.milansky.protocol.bukkit.player.ProtocolPlayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author milansky
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ProtocolPlayerCreateEvent extends ProtocolEvent {
    ProtocolPlayer protocolPlayer;
}
