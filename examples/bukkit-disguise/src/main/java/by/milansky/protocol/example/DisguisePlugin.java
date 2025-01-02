package by.milansky.protocol.example;

import by.milansky.protocol.example.command.DisguiseCommand;
import by.milansky.protocol.example.listener.DisguiseListener;
import by.milansky.protocol.example.player.DisguisePlayerStorage;
import lombok.val;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;

/**
 * @author milansky
 */
@Commands({
        @Command(name = "disguise")
})
@Plugin(name = "Disguise", version = "${version}")
@Description("An example plugin that shows how to use minecraft protocol library")
public final class DisguisePlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        val disguisePlayerStorage = DisguisePlayerStorage.create();

        getServer().getPluginManager().registerEvents(DisguiseListener.create(disguisePlayerStorage), this);
        getServer().getPluginCommand("disguise").setExecutor(DisguiseCommand.create(disguisePlayerStorage));
    }
}
