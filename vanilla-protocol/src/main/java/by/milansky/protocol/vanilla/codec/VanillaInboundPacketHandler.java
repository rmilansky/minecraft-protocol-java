package by.milansky.protocol.vanilla.codec;

import by.milansky.protocol.api.packet.handler.PacketHandler;
import by.milansky.protocol.api.packet.registry.ProtocolStateRegistry;
import by.milansky.protocol.api.state.ProtocolState;
import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.vanilla.utility.ProtocolUtility;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
@ExtensionMethod({ProtocolUtility.class})
@RequiredArgsConstructor(staticName = "create")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class VanillaInboundPacketHandler extends ChannelInboundHandlerAdapter {
    ProtocolVersion version;
    ProtocolStateRegistry stateRegistry;
    PacketHandler handler;

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (!(msg instanceof ByteBuf byteBuf)) {
            super.channelRead(ctx, msg);
            return;
        }

        val copiedBuf = Unpooled.copiedBuffer(byteBuf);
        val identifier = byteBuf.readVarInt();

        val clientboundRegistry = stateRegistry.serverbound(ProtocolState.PLAY);
        if (clientboundRegistry == null) {
            super.channelRead(ctx, copiedBuf);
            byteBuf.release();
            return;
        }

        val packetClass = clientboundRegistry.getPacketById(version, identifier);
        if (packetClass == null) {
            super.channelRead(ctx, copiedBuf);
            byteBuf.release();
            return;
        }

        var packet = packetClass.getDeclaredConstructor().newInstance();
        packet.decode(byteBuf, version);

        val handleResult = handler.handle(packet);

        if (handleResult.cancelled()) return;

        val replacement = handleResult.replacement();
        if (replacement == null) {
            super.channelRead(ctx, copiedBuf);
            byteBuf.release();
            return;
        }

        copiedBuf.release();
        byteBuf.clear();

        byteBuf.writeVarInt(identifier);
        replacement.encode(byteBuf, version);

        super.channelRead(ctx, byteBuf);
    }
}
