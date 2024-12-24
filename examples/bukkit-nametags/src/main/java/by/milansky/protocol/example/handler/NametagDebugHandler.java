package by.milansky.protocol.example.handler;

import by.milansky.protocol.api.packet.handler.PacketHandleResult;
import by.milansky.protocol.base.packet.handler.BasePacketHandleResult;
import by.milansky.protocol.base.packet.handler.annotation.PacketProcessor;
import by.milansky.protocol.vanilla.standard.ClientboundTeam;
import by.milansky.protocol.vanilla.standard.ServerboundTabcomplete;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
@Log4j2
@NoArgsConstructor(staticName = "create")
public final class NametagDebugHandler {
    @PacketProcessor
    public @NotNull PacketHandleResult handle(final ClientboundTeam team) {
        // Логируем, что сервер пытается отправить пакет
        log.info("Outbound team packet: {}", team);

        if (team.containsPlayer("milanskyy")) {
            // Предотвращаем отправку пакета
            return BasePacketHandleResult.cancel();
        }

        // Всё окей, просто разрешаем его отправку
        return BasePacketHandleResult.ok();
    }

    @PacketProcessor
    public @NotNull PacketHandleResult handle(final ServerboundTabcomplete tabcomplete) {
        log.info("Inbound tabcomplete packet: {}", tabcomplete);

        return BasePacketHandleResult.cancel();
    }
}
