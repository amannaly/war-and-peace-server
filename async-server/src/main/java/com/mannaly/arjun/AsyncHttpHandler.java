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

public class AsyncHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        QueryStringDecoder query = new QueryStringDecoder(msg.getUri());
        Map<String, List<String>> params = query.parameters();
        List<String> queryText = params.get("q");

        if(queryText != null && !queryText.get(0).isEmpty()) {
            String text = queryText.get(0);
            AsyncFilePatternSearcher.search(text, ctx);
        }
        else {
            ByteBuf responseText = Unpooled.copiedBuffer("<h4>got request</h4>", CharsetUtil.UTF_8);
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    responseText);
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);;
        }
    }
}
