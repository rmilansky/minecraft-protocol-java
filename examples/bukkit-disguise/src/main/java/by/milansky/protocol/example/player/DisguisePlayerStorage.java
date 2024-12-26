package by.milansky.protocol.example.player;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author milansky
 */
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class DisguisePlayerStorage {
    Map<String, DisguisePlayer> disguisePlayers = new HashMap<>();

    public @NotNull DisguisePlayer disguisePlayer(final @NotNull String originalName) {
        return disguisePlayers.computeIfAbsent(originalName, DisguisePlayer::create);
    }

    public @NotNull DisguisePlayer disguisePlayer(final @NotNull Player player) {
        return disguisePlayer(player.getName());
    }
}
