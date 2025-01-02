package by.milansky.protocol.bukkit;

import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.base.packet.handler.annotation.AnnotationBasedHandler;
import by.milansky.protocol.bukkit.handler.ProtocolPlayerChannelHandler;
import by.milansky.protocol.bukkit.injector.Injector;
import by.milansky.protocol.bukkit.injector.standard.ReflectionInjector;
import by.milansky.protocol.bukkit.listener.ProtocolPlayerListener;
import by.milansky.protocol.vanilla.channel.VanillaChannelInitializer;
import by.milansky.protocol.vanilla.version.VanillaProtocolVersion;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
@Log4j2
@Plugin(name = "Protocol", version = "${version}")
public class ProtocolBukkit extends JavaPlugin {
    private Injector injector;

    private static @NotNull ProtocolVersion defineServerVersion() {
        val bukkitVersion = Bukkit.getBukkitVersion();
        val globalVersion = bukkitVersion.split("-")[0];

        return VanillaProtocolVersion.versionByName(globalVersion);
    }

    @Override
    public void onEnable() {
        val playerChannelHandler = ProtocolPlayerChannelHandler.create();
        val serverVersion = defineServerVersion();

        if (serverVersion == VanillaProtocolVersion.UNKNOWN) {
            log.info("Failed to define the server version, shutting down!..");
            Bukkit.shutdown();
            return;
        }

        injector = ReflectionInjector.create(VanillaChannelInitializer.create(serverVersion, AnnotationBasedHandler.create(playerChannelHandler)));
        injector.inject();

        getServer().getPluginManager().registerEvents(ProtocolPlayerListener.create(playerChannelHandler), this);
    }

    @Override
    public void onDisable() {
        injector.uninject();
    }
}
