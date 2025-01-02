package by.milansky.protocol.example;

import by.milansky.protocol.example.listener.NametagListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;

/**
 * @author milansky
 */
@Plugin(name = "Nametags", version = "${version}")
@Description("An example plugin that shows how to use minecraft protocol library")
public final class NametagPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(NametagListener.create(), this);
    }
}
