package by.milansky.protocol.bukkit;

import by.milansky.protocol.bukkit.listener.PlayerListener;
import lombok.extern.log4j.Log4j2;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.Plugin;

/**
 * @author milansky
 */
@Log4j2
@Plugin(name = "Protocol", version = "1.0.0-ALPHA")
public class ProtocolBukkit extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(PlayerListener.create(this), this);
    }
}
