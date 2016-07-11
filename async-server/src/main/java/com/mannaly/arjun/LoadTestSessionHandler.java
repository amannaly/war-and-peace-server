package com.mannaly.arjun;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;

/**
 * @author Arjun Mannaly
 */
@ChannelHandler.Sharable
public class LoadTestSessionHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final DefaultHttpResponse response =
            new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        String uri = msg.getUri();
        switch (uri) {
            case "/start_session":
                ResponseTimeHandler.startSession();
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                break;
            case "/end_session":
                ResponseTimeHandler.endSession();
                ResponseTimeHandler.startSession();
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                break;
            default:
                // message will be forwarded to AsycRequestHandler,
                // which will also try to release it after channelRead.
                // to prevent an error we increment reference count.
                ReferenceCountUtil.retain(msg);
                ctx.fireChannelRead(msg);
        }
    }
}
