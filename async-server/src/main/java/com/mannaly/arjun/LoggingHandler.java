package com.mannaly.arjun;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.SocketAddress;

public class LoggingHandler extends ChannelDuplexHandler {

    private final static Logger logger = LoggerFactory.getLogger(LoggingHandler.class);

    private long registerStartTime = 0;
    private long startTime = 0;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx)
            throws Exception {
        registerStartTime = System.currentTimeMillis();
        //logger.info(format(ctx, "REGISTERED"));
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx)
            throws Exception {
        //logger.info(format(ctx, "UNREGISTERED"));
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {
        //logger.info(format(ctx, "ACTIVE"));
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)
            throws Exception {
        //logger.info(format(ctx, "INACTIVE"));
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) throws Exception {
        //logger.info(format(ctx, "EXCEPTION: " + cause), cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx,
                                   Object evt) throws Exception {
        //logger.info(format(ctx, "USER_EVENT: " + evt));
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void bind(ChannelHandlerContext ctx,
                     SocketAddress localAddress, ChannelPromise promise) throws Exception {
        //logger.info(format(ctx, "BIND(" + localAddress + ')'));
        super.bind(ctx, localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx,
                        SocketAddress remoteAddress, SocketAddress localAddress,
                        ChannelPromise promise) throws Exception {
        //logger.info(format(ctx, "CONNECT(" + remoteAddress + ", " + localAddress + ')'));
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx,
                           ChannelPromise promise) throws Exception {
        //logger.info(format(ctx, "DISCONNECT()"));
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx,
                      ChannelPromise promise) throws Exception {
        //logger.info(format(ctx, "CLOSE()"));
        super.close(ctx, promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx,
                           ChannelPromise promise) throws Exception {
        //logger.info(format(ctx, "DEREGISTER()"));
        super.deregister(ctx, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //logger.info(format(ctx, "RECEIVED"));
        long time = System.currentTimeMillis() - registerStartTime;
        logger.info("received in {} ms", time);
        startTime = System.currentTimeMillis();
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //logger.info(format(ctx, "WRITE"));

        ctx.write(msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        //logger.info(format(ctx, "FLUSH"));
        long time = System.currentTimeMillis() - startTime;
        logger.info("request time is {} ms", time);
        ctx.flush();
    }

    protected String format(ChannelHandlerContext ctx, String message) {
        String chStr = ctx.channel().toString();
        return new StringBuilder(chStr.length() + message.length() + 1)
                .append(chStr)
                .append(' ')
                .append(message)
                .toString();
    }
}
