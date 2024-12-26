package by.milansky.protocol.vanilla.channel;

import by.milansky.protocol.api.packet.handler.PacketHandler;
import by.milansky.protocol.api.state.ProtocolState;
import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.base.packet.handler.BaseMergedPacketHandler;
import by.milansky.protocol.base.packet.handler.annotation.AnnotationBasedHandler;
import by.milansky.protocol.vanilla.codec.VanillaInboundPacketHandler;
import by.milansky.protocol.vanilla.codec.VanillaOutboundPacketHandler;
import by.milansky.protocol.vanilla.codec.VanillaPacketEncoder;
import by.milansky.protocol.vanilla.handler.VanillaStateTrackingHandler;
import by.milansky.protocol.vanilla.registry.VanillaStateRegistry;
import by.milansky.protocol.vanilla.utility.ChannelUtility;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
@Log4j2
@ExtensionMethod({ChannelUtility.class})
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class VanillaChannelInitializer extends ChannelInitializer<Channel> {
    private static final String CUSTOM_ENCODER_PIPELINE_NAME = "milansky-protocol-encoder";

    ProtocolVersion version;
    PacketHandler[] additionalHandlers;

    @Contract("_, _ -> new")
    public static @NotNull VanillaChannelInitializer create(
            final @NotNull ProtocolVersion version,
            final @NotNull PacketHandler @NotNull ... additionalHandlers
    ) {
        return new VanillaChannelInitializer(version, additionalHandlers);
    }

    @Override
    public void initChannel(final @NotNull Channel channel) {
        val pipeline = channel.pipeline();
        val packetHandler = BaseMergedPacketHandler.create(AnnotationBasedHandler.create(VanillaStateTrackingHandler.create()));

        packetHandler.append(additionalHandlers);

        channel.updateProtocolState(ProtocolState.HANDSHAKE);
        channel.updatePacketHandler(packetHandler);

        val decoderName = pipeline.names().contains("inbound_config") ? "inbound_config" : "decoder";
        val encoderName = pipeline.names().contains("outbound_config") ? "outbound_config" : "encoder";

        val encoder = (MessageToByteEncoder<?>) channel.pipeline().get(encoderName);
        val outboundHandler = VanillaOutboundPacketHandler.create(
                version, VanillaStateRegistry.standardRegistry(),
                packetHandler, encoder
        );

        pipeline.replace(encoder, encoderName, outboundHandler);

        val decoder = (ByteToMessageDecoder) channel.pipeline().get(decoderName);
        val inboundHandler = VanillaInboundPacketHandler.create(
                version, VanillaStateRegistry.standardRegistry(), decoder
        );

        pipeline.replace(decoder, decoderName, inboundHandler);

        pipeline.addAfter(encoderName, CUSTOM_ENCODER_PIPELINE_NAME,
                VanillaPacketEncoder.create(version, VanillaStateRegistry.standardRegistry()));
    }
}
