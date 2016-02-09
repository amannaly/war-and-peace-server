package com.mannaly.arjun;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;


public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private final EventExecutorGroup executor = new DefaultEventExecutorGroup(50);

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipe = ch.pipeline();

        pipe.addLast(new HttpRequestDecoder());
        pipe.addLast(new HttpObjectAggregator(1024));
        pipe.addLast(new HttpResponseEncoder());
        pipe.addLast(new HttpHandler(executor));
    }




}
