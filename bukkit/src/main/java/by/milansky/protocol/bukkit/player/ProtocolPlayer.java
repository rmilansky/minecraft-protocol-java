package by.milansky.protocol.bukkit.player;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.packet.handler.PacketHandler;
import by.milansky.protocol.base.packet.handler.BaseMergedPacketHandler;
import by.milansky.protocol.vanilla.codec.VanillaOutboundPacketHandler;
import by.milansky.protocol.vanilla.codec.VanillaPacketEncoder;
import by.milansky.protocol.vanilla.registry.VanillaStateRegistry;
import by.milansky.protocol.vanilla.version.VanillaProtocolVersion;
import io.netty.channel.Channel;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.log4j.Log4j2;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author milansky
 */
@Log4j2
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ProtocolPlayer {
    private static final Class<?> CRAFT_PLAYER_CLASS, ENTITY_PLAYER_CLASS, PLAYER_CONNECTION_CLASS, NETWORK_MANAGER_CLASS;
    private static final Field PLAYER_CONNECTION_FIELD, NETWORK_MANAGER_FIELD, NETWORK_MANAGER_CHANNEL_FIELD;
    private static final Method CRAFT_PLAYER_HANDLE;

    static {
        try {
            val obcPrefix = Bukkit.getServer().getClass().getPackage().getName();

            val nmsPrefix = obcPrefix.replace("org.bukkit.craftbukkit", "net.minecraft.server");
            val version = obcPrefix.replace("org.bukkit.craftbukkit", "").replace(".", "");

            log.info("Spigot reflect version: {}", version);
            log.info("OBC Prefix: {}", obcPrefix);
            log.info("NMS Prefix: {}", nmsPrefix);

            CRAFT_PLAYER_CLASS = Class.forName(obcPrefix + ".entity.CraftPlayer");
            ENTITY_PLAYER_CLASS = Class.forName(nmsPrefix + ".EntityPlayer");
            PLAYER_CONNECTION_CLASS = Class.forName(nmsPrefix + ".PlayerConnection");
            NETWORK_MANAGER_CLASS = Class.forName(nmsPrefix + ".NetworkManager");
            CRAFT_PLAYER_HANDLE = CRAFT_PLAYER_CLASS.getDeclaredMethod("getHandle");

            log.info("CraftPlayer class found: {}", CRAFT_PLAYER_CLASS);
            log.info("CraftPlayer method getHandle() found: {}", CRAFT_PLAYER_HANDLE);

            PLAYER_CONNECTION_FIELD = fieldByType(ENTITY_PLAYER_CLASS, PLAYER_CONNECTION_CLASS);
            NETWORK_MANAGER_FIELD = fieldByType(PLAYER_CONNECTION_CLASS, NETWORK_MANAGER_CLASS);
            NETWORK_MANAGER_CHANNEL_FIELD = fieldByType(NETWORK_MANAGER_CLASS, Channel.class);

            log.info("Player Connection class found: {} ", PLAYER_CONNECTION_CLASS);
            log.info("Network Manager Field found: {} ", PLAYER_CONNECTION_CLASS);
        } catch (final Throwable throwable) {
            log.catching(throwable);

            throw new RuntimeException(throwable);
        }
    }

    @Getter
    Player nativePlayer;
    @NonFinal
    BaseMergedPacketHandler packetHandler = BaseMergedPacketHandler.create();

    private static @NotNull Field fieldByType(final Class<?> clazz, final Class<?> fieldClass) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getType() == fieldClass)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find field " + fieldClass + " in class " + clazz));
    }

    @SneakyThrows
    public Channel channel() {
        return (Channel) NETWORK_MANAGER_CHANNEL_FIELD.get(NETWORK_MANAGER_FIELD.get(PLAYER_CONNECTION_FIELD.get(CRAFT_PLAYER_HANDLE.invoke(nativePlayer))));
    }

    public void appendPacketHandler(final @NotNull PacketHandler packetHandler) {
        this.packetHandler.append(packetHandler);
    }

    public void appendPipeline() {
        val channel = channel();
        val outboundHandler = VanillaOutboundPacketHandler.create(
                VanillaProtocolVersion.MINECRAFT_1_12_2,
                VanillaStateRegistry.standardRegistry(), packetHandler
        );

        channel.pipeline().addBefore("encoder", "milansky-protocol-decoder", outboundHandler);
        channel.pipeline().addAfter("encoder", "milansky-protocol-encoder",
                VanillaPacketEncoder.create(VanillaProtocolVersion.MINECRAFT_1_12_2, VanillaStateRegistry.standardRegistry()));
    }

    public void sendPacket(final @NotNull Packet packet) {
        channel().writeAndFlush(packet);
    }
}
