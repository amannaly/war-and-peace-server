package com.mannaly.arjun;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author Arjun Mannaly
 */
@ChannelHandler.Sharable
public class ResponseTimeHandler extends ChannelInboundHandlerAdapter {

    private final static Logger logger  = LoggerFactory.getLogger(ResponseTimeHandler.class);

    public static final AttributeKey<Long> START_TIME_KEY = AttributeKey.valueOf("startTime");

    private static volatile Histogram histogram;

    private static LongAdder connectionCount = new LongAdder();

    private static long sessionStartTime;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        connectionCount.increment();
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Attribute<Long> startTime = ctx.channel().attr(START_TIME_KEY);
        startTime.set(System.currentTimeMillis());
        ctx.fireChannelRead(msg);
    }

    public static void recordResponseTime(long responseTime) {
        if (histogram != null)
            histogram.recordValue(responseTime);
    }

    public static void startSession() {
        logger.info("Started load testing session");
        histogram = new ConcurrentHistogram(10_000L, 3);
        connectionCount.reset();
        sessionStartTime = System.currentTimeMillis();
    }

    public static void endSession() {
        long sessionEndTime = System.currentTimeMillis();
        logger.info("\n\n************** COMPLETED SESSION *************");
        logger.info("Session duration: {}", (sessionEndTime - sessionStartTime)/1000);
        logger.info("Number of connections: {}", connectionCount.longValue());
        logger.info("Throughput: {} req/sec", histogram.getTotalCount() / ((sessionEndTime - sessionStartTime)/1000.0));
        logger.info("Number of requests: {}", histogram.getTotalCount());
        logger.info("Mean response time: {} ms", histogram.getMean());
        logger.info("95th percentile response time: {} ms", histogram.getValueAtPercentile(95.0));
        logger.info("99th percentile response time: {} ms", histogram.getValueAtPercentile(99.0));
        logger.info("Max response time: {} ms\n\n", histogram.getMaxValueAsDouble());
        histogram = null;
        connectionCount.reset();
    }
}
