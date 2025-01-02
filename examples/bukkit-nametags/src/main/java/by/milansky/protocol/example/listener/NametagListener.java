package by.milansky.protocol.example.listener;

import by.milansky.protocol.base.packet.handler.annotation.AnnotationBasedHandler;
import by.milansky.protocol.bukkit.event.ProtocolPlayerCreateEvent;
import by.milansky.protocol.example.handler.NametagDebugHandler;
import by.milansky.protocol.vanilla.standard.ClientboundTeam;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author milansky
 */
@Log4j2
@NoArgsConstructor(staticName = "create")
public final class NametagListener implements Listener {
    @EventHandler
    public void onPlayerCreate(final ProtocolPlayerCreateEvent event) {
        val protocolPlayer = event.protocolPlayer();

        protocolPlayer.appendPacketHandler(AnnotationBasedHandler.create(NametagDebugHandler.create()));

        protocolPlayer.sendPacket(new ClientboundTeam(
                "test_team",
                ClientboundTeam.TeamMode.CREATE,
                Component.empty(),
                Component.text("GOOD ", NamedTextColor.AQUA, TextDecoration.BOLD),
                Component.text(" JOB", NamedTextColor.GREEN, TextDecoration.BOLD),
                ClientboundTeam.NameTagVisibility.ALWAYS,
                ClientboundTeam.CollisionRule.NEVER,
                NamedTextColor.WHITE,
                (byte) 0, new String[]{protocolPlayer.nativePlayer().getName()}
        ));
    }
}
