package by.milansky.protocol.base.packet.handler.annotation;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.packet.handler.PacketHandleResult;
import by.milansky.protocol.api.packet.handler.PacketHandler;
import by.milansky.protocol.base.packet.handler.BaseMergedPacketHandler;
import by.milansky.protocol.base.packet.handler.BasePacketHandleResult;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author milansky
 */
@Log4j2
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class AnnotationBasedHandler implements PacketHandler {
    Map<Class<?>, PacketHandler> handlers;

    @Contract("_ -> new")
    public static PacketHandler create(final Object... handlers) {
        val annotationHandlers = new HashMap<Class<?>, PacketHandler>();

        for (val handler : handlers) {
            val clazz = handler.getClass();

            for (val declaredMethod : clazz.getDeclaredMethods()) {
                if (!declaredMethod.isAnnotationPresent(PacketProcessor.class)) continue;

                if (declaredMethod.getParameterCount() != 2) {
                    throw new IllegalStateException("Method is marked as @PacketProcessor, but has too many parameters");
                }

                val channelArgumentType = declaredMethod.getParameterTypes()[0];
                val packetArgumentType = declaredMethod.getParameterTypes()[1];

                if (channelArgumentType != Channel.class ||
                        !Packet.class.isAssignableFrom(packetArgumentType)) {
                    throw new IllegalStateException("Method is marked as @PacketProcessor, but has no packet argument");
                }

                if (!PacketHandleResult.class.isAssignableFrom(declaredMethod.getReturnType())) {
                    throw new IllegalStateException("Method is marked as @PacketProcessor, but returns not result type");
                }

                val processorHandler = new PacketHandler() {
                    @Override
                    public @NotNull PacketHandleResult handle(final @NotNull Channel channel, final @NotNull Packet packet) {
                        if (packetArgumentType != Packet.class && packet.getClass() != packetArgumentType)
                            return BasePacketHandleResult.ok();

                        declaredMethod.setAccessible(true);

                        try {
                            return (PacketHandleResult) declaredMethod.invoke(handler, channel, packet);
                        } catch (final InvocationTargetException | IllegalAccessException e) {
                            log.catching(e);
                        } finally {
                            declaredMethod.setAccessible(false);
                        }

                        return BasePacketHandleResult.ok();
                    }
                };

                if (annotationHandlers.containsKey(clazz)) {
                    annotationHandlers.put(packetArgumentType, BaseMergedPacketHandler.create(annotationHandlers.get(clazz), processorHandler));
                    continue;
                }

                annotationHandlers.put(packetArgumentType, processorHandler);
            }
        }

        return new AnnotationBasedHandler(annotationHandlers);
    }

    @Override
    public @NotNull PacketHandleResult handle(final @NotNull Channel channel, final @NotNull Packet packet) {
        val handler = handlers.get(packet.getClass());

        if (handler == null)
            return BasePacketHandleResult.ok();

        return handler.handle(channel, packet);
    }
}
