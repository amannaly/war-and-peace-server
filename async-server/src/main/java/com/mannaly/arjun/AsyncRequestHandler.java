package com.mannaly.arjun;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

public class AsyncRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final ByteBuf lineBreak = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("<br>", CharsetUtil.UTF_8));
    private static final ByteBuf noResult = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("<h4>No query param.</h4>", CharsetUtil.UTF_8));

    private static final int lineBreakSize = lineBreak.readableBytes();
    private static final int noResultSize = noResult.readableBytes();

    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        QueryStringDecoder query = new QueryStringDecoder(msg.getUri());
        Map<String, List<String>> params = query.parameters();
        List<String> queryText = params.get("q");

        final CompositeByteBuf result = Unpooled.compositeBuffer();
        if(queryText != null && !queryText.get(0).isEmpty()) {
            String text = queryText.get(0).toLowerCase();
            List<ByteBuf> matchingLines = InvertedIndex.INSTANCE.find(text);

            matchingLines.forEach(s -> {
                result.addComponent(s);
                result.addComponent(lineBreak);

                // writerIndex in CompositeBuffer is not incremented by addComponent.
                result.writerIndex(result.writerIndex() + s.readableBytes() + lineBreakSize);
            });
        }
        else {
            result.addComponent(noResult);
            result.writerIndex(noResultSize);
        }

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                result);

        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
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
