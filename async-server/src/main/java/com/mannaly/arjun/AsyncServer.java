package com.mannaly.arjun;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class AsyncServer {

    public void run() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            System.out.println("Starting async server ....");

            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 5000)
                    .childOption(ChannelOption.SO_KEEPALIVE, false);

            ChannelFuture f = b.bind(8888).sync();
            f.channel().closeFuture().sync();
            System.out.println("Shutting down server");
        }
        finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        AsyncServer server = new AsyncServer();
        server.run();
    }
}
