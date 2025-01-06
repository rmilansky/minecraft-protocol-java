package by.milansky.protocol.vanilla.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(staticName = "create", access = AccessLevel.PACKAGE)
final class ProxiedOutboundChannelHandler extends ChannelOutboundHandlerAdapter {
    ChannelOutboundHandlerAdapter parent;
    Runnable afterWrite;

    @Override
    public void write(
            final @NotNull ChannelHandlerContext ctx,
            final @NotNull Object msg,
            final @NotNull ChannelPromise promise
    ) throws Exception {
        parent.write(ctx, msg, promise);

        afterWrite.run();
    }
}
