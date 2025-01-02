package by.milansky.protocol.example.command;

import by.milansky.protocol.example.player.DisguisePlayerStorage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class DisguiseCommand implements CommandExecutor {
    DisguisePlayerStorage storage;

    @Override
    public boolean onCommand(
            final @NotNull CommandSender commandSender,
            final @NotNull Command command,
            final @NotNull String label,
            final @NotNull String @NotNull [] arguments
    ) {
        if (!(commandSender instanceof Player player)) return false;

        val disguisePlayer = storage.disguisePlayer(player);

        if (arguments.length != 1) {
            player.sendMessage("/disguise <name/reset>");
            return true;
        }

        val argument = arguments[0];

        if (argument.equalsIgnoreCase("reset")) {
            disguisePlayer.updateFakeName(null);
            return true;
        }

        disguisePlayer.updateFakeName(argument);

        return true;
    }
}
