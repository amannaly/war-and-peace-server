package com.mannaly.arjun;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

public class AsyncServer {

    public void run() throws Exception {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

        OsType osType = OsType.getOsType();

        EventLoopGroup boss = getBossGroup(osType);
        EventLoopGroup worker = getWorkerGroup(osType);

        try {
            System.out.println("Starting async server ....");

            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(getChannelType(osType))
                    .childHandler(new ServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 20000)
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

    private EventLoopGroup getBossGroup(OsType osType) {
        switch (osType) {
            case LINUX: return new EpollEventLoopGroup();
            default:    return new NioEventLoopGroup();
        }
    }

    private EventLoopGroup getWorkerGroup(OsType osType) {
        switch (osType) {
            case LINUX: return new EpollEventLoopGroup();
            default:    return new NioEventLoopGroup();
        }
    }

    private Class<? extends ServerChannel> getChannelType(OsType osType) {
        switch (osType) {
            case LINUX: return EpollServerSocketChannel.class;
            default:    return NioServerSocketChannel.class;
        }
    }

    public static void main(String[] args) throws Exception {
        InvertedIndex.INSTANCE.buildCache();
        AsyncServer server = new AsyncServer();
        server.run();
    }
}
