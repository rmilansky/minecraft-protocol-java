package by.milansky.protocol.base.packet.handler.annotation;

import by.milansky.protocol.api.packet.Packet;
import by.milansky.protocol.api.packet.handler.PacketHandleResult;
import by.milansky.protocol.api.packet.handler.PacketHandler;
import by.milansky.protocol.base.packet.handler.BaseMergedPacketHandler;
import by.milansky.protocol.base.packet.handler.BasePacketHandleResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import lombok.val;
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

    public static PacketHandler create(final Object... handlers) {
        val annotationHandlers = new HashMap<Class<?>, PacketHandler>();

        for (val handler : handlers) {
            val clazz = handler.getClass();

            for (val declaredMethod : clazz.getDeclaredMethods()) {
                if (!declaredMethod.isAnnotationPresent(PacketProcessor.class)) continue;

                if (declaredMethod.getParameterCount() != 1) {
                    throw new IllegalStateException("Method is marked as @PacketProcessor, but has too many parameters");
                }

                val argumentType = declaredMethod.getParameterTypes()[0];

                if (!Packet.class.isAssignableFrom(argumentType)) {
                    throw new IllegalStateException("Method is marked as @PacketProcessor, but has no packet argument");
                }

                if (!PacketHandleResult.class.isAssignableFrom(declaredMethod.getReturnType())) {
                    throw new IllegalStateException("Method is marked as @PacketProcessor, but returns not result type");
                }

                val processorHandler = new PacketHandler() {
                    @Override
                    public @NotNull PacketHandleResult handle(final @NotNull Packet packet) {
                        if (argumentType != Packet.class && packet.getClass() != argumentType)
                            return BasePacketHandleResult.ok();

                        declaredMethod.setAccessible(true);

                        try {
                            return (PacketHandleResult) declaredMethod.invoke(handler, packet);
                        } catch (final InvocationTargetException | IllegalAccessException e) {
                            log.catching(e);
                        } finally {
                            declaredMethod.setAccessible(false);
                        }

                        return BasePacketHandleResult.ok();
                    }
                };

                if (annotationHandlers.containsKey(clazz)) {
                    annotationHandlers.put(argumentType, BaseMergedPacketHandler.create(annotationHandlers.get(clazz), processorHandler));
                    continue;
                }

                annotationHandlers.put(argumentType, processorHandler);
            }
        }

        return new AnnotationBasedHandler(annotationHandlers);
    }

    @Override
    public @NotNull PacketHandleResult handle(final @NotNull Packet packet) {
        val handler = handlers.get(packet.getClass());

        if (handler == null)
            return BasePacketHandleResult.ok();

        return handler.handle(packet);
    }
}
