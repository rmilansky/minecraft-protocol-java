package by.milansky.protocol.bukkit.injector.standard;

import by.milansky.protocol.bukkit.injector.Injector;
import by.milansky.protocol.vanilla.channel.VanillaChannelInitializer;
import io.netty.channel.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * @author milansky
 */
@Log4j2
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ReflectionInjector implements Injector {
    private static final Class<?> MINECRAFT_SERVER_CLASS, SERVER_CONNECTION_CLASS, CRAFT_SERVER_CLASS, NETWORK_MANAGER_CLASS;
    private static final Field MINECRAFT_SERVER_FIELD, SERVER_CONNECTION_FIELD, NETWORK_MANAGER_CHANNEL_FIELD;

    static {
        try {
            val obcPrefix = Bukkit.getServer().getClass().getPackage().getName();
            val nmsPrefix = obcPrefix.replace("org.bukkit.craftbukkit", "net.minecraft.server");

            MINECRAFT_SERVER_CLASS = findClass(nmsPrefix + ".MinecraftServer", "net.minecraft.server.MinecraftServer");
            SERVER_CONNECTION_CLASS = findClass(nmsPrefix + ".ServerConnection", "net.minecraft.server.network.ServerConnection");
            NETWORK_MANAGER_CLASS = findClass(nmsPrefix + ".NetworkManager", "net.minecraft.network.NetworkManager");
            CRAFT_SERVER_CLASS = findClass(obcPrefix + ".CraftServer");

            MINECRAFT_SERVER_FIELD = hasFieldOfType(CRAFT_SERVER_CLASS, MINECRAFT_SERVER_CLASS)
                    ? fieldByType(CRAFT_SERVER_CLASS, MINECRAFT_SERVER_CLASS)
                    : fieldByType(MINECRAFT_SERVER_CLASS, MINECRAFT_SERVER_CLASS);

            SERVER_CONNECTION_FIELD = fieldByType(MINECRAFT_SERVER_CLASS, SERVER_CONNECTION_CLASS);
            NETWORK_MANAGER_CHANNEL_FIELD = fieldByType(NETWORK_MANAGER_CLASS, Channel.class);
        } catch (final Throwable throwable) {
            log.catching(throwable);

            throw new RuntimeException(throwable);
        }
    }

    VanillaChannelInitializer channelInitializer;

    private static @NotNull Class<?> findClass(final @NotNull String... names) throws ClassNotFoundException {
        for (val name : names) {
            try {
                return Class.forName(name);
            } catch (final ClassNotFoundException ignored) {
            }
        }

        throw new ClassNotFoundException();
    }

    private static @NotNull Field fieldByType(final Class<?> clazz, final Class<?> fieldClass) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getType() == fieldClass)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find field " + fieldClass + " in class " + clazz));
    }

    private static boolean hasFieldOfType(final Class<?> clazz, final Class<?> fieldClass) {
        for (val declaredField : clazz.getDeclaredFields())
            if (declaredField.getType() == fieldClass) return true;

        return false;
    }

    @Override
    @SneakyThrows
    public void inject() {
        MINECRAFT_SERVER_FIELD.setAccessible(true);
        SERVER_CONNECTION_FIELD.setAccessible(true);

        val minecraftServer = MINECRAFT_SERVER_FIELD.get(Bukkit.getServer());
        val serverConnection = SERVER_CONNECTION_FIELD.get(minecraftServer);

        for (val declaredField : serverConnection.getClass().getDeclaredFields()) {
            if (declaredField.getType() != List.class) continue;

            declaredField.setAccessible(true);

            val list = (List<?>) declaredField.get(serverConnection);

            for (val object : list) {
                val clazz = object.getClass();

                if (clazz == NETWORK_MANAGER_CLASS) {
                    val channel = (Channel) NETWORK_MANAGER_CHANNEL_FIELD.get(object);

                    channelInitializer.initChannel(channel);
                } else if (ChannelFuture.class.isAssignableFrom(clazz)) {
                    val future = (ChannelFuture) object;

                    future.channel().pipeline().addFirst(ServerBootstrapChannelHandler.create(list, channelInitializer));
                }
            }
        }
    }

    @Override
    public void uninject() {
        Bukkit.shutdown();
    }

    @RequiredArgsConstructor(staticName = "create")
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class ServerBootstrapChannelHandler extends ChannelInboundHandlerAdapter {
        List<?> synchronizationList;
        VanillaChannelInitializer channelInitializer;

        @Override
        public void channelRead(
                final @NotNull ChannelHandlerContext ctx,
                final @NotNull Object msg
        ) {
            if (!(msg instanceof Channel clientChannel)) return;

            clientChannel.pipeline().addFirst(new ChannelInitializer<>() {
                @Override
                protected void initChannel(final @NotNull Channel channel) {
                    synchronized (synchronizationList) {
                        channel.eventLoop().execute(() -> channelInitializer.initChannel(clientChannel));
                    }
                }
            });

            ctx.fireChannelRead(msg);
        }
    }
}
