package com.mannaly.arjun;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;

public class AsyncRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        QueryStringDecoder query = new QueryStringDecoder(msg.getUri());
        Map<String, List<String>> params = query.parameters();
        List<String> queryText = params.get("q");

        String responseText;
        if(queryText != null && !queryText.get(0).isEmpty()) {
            String text = queryText.get(0).toLowerCase();
            List<String> matchingLines = InvertedIndex.INSTANCE.find(text);

            StringBuilder builder = new StringBuilder();
            for (String s : matchingLines) {
                builder.append(s);
                builder.append("<br>");
            }

            responseText = builder.toString();
        }
        else {
            responseText = "<h4>No query param.</h4>";
        }

        ByteBuf buf = Unpooled.copiedBuffer(responseText, CharsetUtil.UTF_8);
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                buf);

        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
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
