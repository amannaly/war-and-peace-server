package com.mannaly.arjun;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private final EventExecutorGroup executor = new DefaultEventExecutorGroup(50);

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeLine = ch.pipeline();

        pipeLine.addLast(new HttpRequestDecoder());
        pipeLine.addLast(new HttpObjectAggregator(1024));
        pipeLine.addLast(new HttpResponseEncoder());
        pipeLine.addLast("logging", new LoggingHandler(LogLevel.DEBUG));
        pipeLine.addLast(executor, new AsyncRequestHandler());
        //pipe.addLast(new AsyncHttpHandler());
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("reading channel");
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("reading channel complete");
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel active");
        super.channelActive(ctx);
    }
}
