package com.mannaly.arjun;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;
import io.netty.util.ReferenceCountUtil;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;

/**
 * @author Arjun Mannaly
 */
@ChannelHandler.Sharable
public class EchoHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final DefaultFullHttpResponse response;

    public EchoHandler() {
        response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.EMPTY_BUFFER);

        response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        response.headers().set(CONTENT_LENGTH, 0);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        String uri = msg.getUri();
        switch (uri) {
            case "/echo":
                ChannelFuture writeFuture = ctx.writeAndFlush(response);
                measureResponseTime(writeFuture, ctx);
                break;
            default:
                // message will be forwarded to the next handler,
                // which will also try to release it after channelRead.
                // to prevent an error we increment reference count.
                ReferenceCountUtil.retain(msg);
                ctx.fireChannelRead(msg);
        }
    }

    private void measureResponseTime(ChannelFuture writeFuture, ChannelHandlerContext ctx) {
        writeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                long endTime = System.currentTimeMillis();
                Attribute<Long> startTime = ctx.channel().attr(ResponseTimeHandler.START_TIME_KEY);
                ResponseTimeHandler.recordResponseTime(endTime - startTime.get());
            }
        });
    }
}
