package com.mannaly.arjun;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;

@ChannelHandler.Sharable
public class AsyncRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final static Logger logger  = LoggerFactory.getLogger(AsyncRequestHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {

        QueryStringDecoder query = new QueryStringDecoder(msg.getUri());
        Map<String, List<String>> params = query.parameters();
        List<String> queryText = params.get("q");

        String responseText;
        ByteBuf result;

        if(queryText != null && !queryText.get(0).isEmpty()) {
            String text = queryText.get(0).toLowerCase();

            result = InvertedIndexV2.INSTANCE.find(text);
        }
        else {
            responseText = "<h4>No query param.</h4>";
            result = Unpooled.copiedBuffer(responseText, CharsetUtil.UTF_8);
        }

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                result);

        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(CONTENT_ENCODING, "deflate");
        ChannelFuture writeFuture = ctx.writeAndFlush(response);
        measureResponseTime(writeFuture, ctx);
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("error!!", cause);
        String errorMessage = cause.toString();
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.copiedBuffer(errorMessage, CharsetUtil.UTF_8));

        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
