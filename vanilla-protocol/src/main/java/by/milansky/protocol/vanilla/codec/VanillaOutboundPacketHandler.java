package by.milansky.protocol.vanilla.codec;

import by.milansky.protocol.api.packet.registry.ProtocolStateRegistry;
import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.vanilla.utility.ChannelUtility;
import by.milansky.protocol.vanilla.utility.ProtocolUtility;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * @author milansky
 */
@Log4j2
@Setter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ExtensionMethod({ProtocolUtility.class, ChannelUtility.class})
public final class VanillaOutboundPacketHandler extends MessageToByteEncoder<Object> {
    private static final MethodHandle ENCODE_METHOD_HANDLE;

    static {
        try {
            val lookup = MethodHandles.lookup();

            val declaredMethod = MessageToByteEncoder.class.getDeclaredMethod(
                    "encode", ChannelHandlerContext.class, Object.class, ByteBuf.class
            );
            declaredMethod.setAccessible(true);

            ENCODE_METHOD_HANDLE = lookup.unreflect(declaredMethod);
        } catch (final Throwable throwable) {
            log.catching(throwable);

            throw new RuntimeException(throwable);
        }
    }

    ProtocolVersion version;
    ProtocolStateRegistry stateRegistry;
    @NonFinal
    MessageToByteEncoder<?> downstream;

    @Override
    @SneakyThrows
    protected void encode(
            final @NotNull ChannelHandlerContext ctx,
            final @NotNull Object object,
            final @NotNull ByteBuf byteBuf
    ) throws Exception {
        if (object instanceof ByteBuf otherBuf) {
            byteBuf.writeBytes(otherBuf);
            return;
        }

        ENCODE_METHOD_HANDLE.invokeExact(downstream, ctx, object, byteBuf);

        val copiedBuf = Unpooled.copiedBuffer(byteBuf);

        try {
            val channel = ctx.channel();
            val identifier = copiedBuf.readVarInt();

            val clientboundRegistry = stateRegistry.clientbound(channel.protocolState());
            if (clientboundRegistry == null) return;

            val packetClass = clientboundRegistry.getPacketById(version, identifier);
            if (packetClass == null) return;

            val packet = packetClass.getDeclaredConstructor().newInstance();
            packet.decode(copiedBuf, version);

            val handleResult = channel.packetHandler().handle(channel, packet);

            if (handleResult.cancelled()) {
                byteBuf.clear();
                return;
            }

            val replacement = handleResult.replacement();
            if (replacement == null) return;

            byteBuf.clear();

            byteBuf.writeVarInt(identifier);
            replacement.encode(byteBuf, version);
        } catch (final Throwable throwable) {
            log.catching(throwable);
        } finally {
            copiedBuf.release();
        }
    }
}
