package com.mannaly.arjun;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    //private final EventExecutorGroup executor = new DefaultEventExecutorGroup(100);

    private final LoadTestSessionHandler sessionHandler = new LoadTestSessionHandler();
    private final AsyncRequestHandler asyncRequestHandler = new AsyncRequestHandler();
    private final ResponseTimeHandler responseTimeHandler = new ResponseTimeHandler();
    private final EchoHandler echoHandler = new EchoHandler();

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeLine = ch.pipeline();

        pipeLine.addLast("response time", responseTimeHandler);
        pipeLine.addLast("decoder", new HttpRequestDecoder());
        pipeLine.addLast("aggregator", new HttpObjectAggregator(1024));
        pipeLine.addLast("encoder", new HttpResponseEncoder());
        pipeLine.addLast("echo endpoint", echoHandler);
        pipeLine.addLast("load testing session", sessionHandler);
        pipeLine.addLast("async handler", asyncRequestHandler);
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
