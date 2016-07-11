package com.mannaly.arjun;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Arjun Mannaly
 */
@ChannelHandler.Sharable
public class RequestCounter extends ChannelDuplexHandler {

    private static LongAdder requestCounter = new LongAdder();
    private static final Logger logger = LoggerFactory.getLogger(RequestCounter.class);

    private final static ExecutorService thread = Executors.newSingleThreadExecutor();


    public RequestCounter() {
        thread.submit(() -> {
            while (true) {
                if (requestCounter.longValue() > 1) {
                    logger.info("Request count - {}", requestCounter.longValue());
                }
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        requestCounter.increment();
        ctx.fireChannelRead(msg);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        requestCounter.decrement();
        ctx.flush();
    }
}
