package by.milansky.protocol.vanilla.codec;

import by.milansky.protocol.api.packet.registry.ProtocolStateRegistry;
import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.vanilla.utility.ChannelUtility;
import by.milansky.protocol.vanilla.utility.ProtocolUtility;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * @author milansky
 */
@Log4j2
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ExtensionMethod({ProtocolUtility.class, ChannelUtility.class})
public final class VanillaInboundPacketHandler extends ByteToMessageDecoder {
    private static final MethodHandle DECODE_METHOD_HANDLE;

    static {
        try {
            val lookup = MethodHandles.lookup();

            val declaredMethod = ByteToMessageDecoder.class.getDeclaredMethod(
                    "decode", ChannelHandlerContext.class, ByteBuf.class, List.class
            );
            declaredMethod.setAccessible(true);

            DECODE_METHOD_HANDLE = lookup.unreflect(declaredMethod);
        } catch (final Throwable throwable) {
            log.catching(throwable);

            throw new RuntimeException(throwable);
        }
    }

    ProtocolVersion version;
    ProtocolStateRegistry stateRegistry;
    ByteToMessageDecoder downstream;

    @Override
    @SneakyThrows
    protected void decode(
            final @NotNull ChannelHandlerContext ctx,
            final @NotNull ByteBuf byteBuf,
            final @NotNull List<Object> list
    ) {
        val copiedBuf = Unpooled.copiedBuffer(byteBuf);
        boolean cancelled = false;

        try {
            val channel = ctx.channel();
            val identifier = copiedBuf.readVarInt();

            val clientboundRegistry = stateRegistry.serverbound(channel.protocolState());
            if (clientboundRegistry == null) return;

            val packetClass = clientboundRegistry.getPacketById(version, identifier);
            if (packetClass == null) return;

            val packet = packetClass.getDeclaredConstructor().newInstance();
            packet.decode(copiedBuf, version);

            val handleResult = channel.packetHandler().handle(channel, packet);

            if (cancelled = handleResult.cancelled()) return;

            val replacement = handleResult.replacement();
            if (replacement == null) return;

            byteBuf.clear();

            byteBuf.writeVarInt(identifier);
            replacement.encode(byteBuf, version);
        } catch (final Throwable throwable) {
            log.catching(throwable);
        } finally {
            if (!cancelled) DECODE_METHOD_HANDLE.invoke(downstream, ctx, byteBuf, list);

            copiedBuf.release();
            byteBuf.skipBytes(byteBuf.readableBytes());
        }
    }
}
