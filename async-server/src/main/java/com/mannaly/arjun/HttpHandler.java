package com.mannaly.arjun;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import java.util.List;
import java.util.Map;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


//    private final EventExecutorGroup executor;
//
//    public HttpHandler(EventExecutorGroup executor) {
//        //System.out.println("Creating HttpHandler");
//        this.executor = executor;
//    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        QueryStringDecoder query = new QueryStringDecoder(msg.getUri());
        Map<String, List<String>> params = query.parameters();
        List<String> queryText = params.get("q");

        if(queryText != null && !queryText.get(0).isEmpty()) {

            String text = queryText.get(0);
            FilePatternSearcher searcher = new FilePatternSearcher(text);
            List<String> matchingLines = searcher.call();
            StringBuilder builder = new StringBuilder();
            for (String s : matchingLines) {
                builder.append(s);
                builder.append("<br>");
            }
            ByteBuf responseText = Unpooled.copiedBuffer(builder.toString(), CharsetUtil.UTF_8);
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    responseText);
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);



//            final Future<List<String>> f = executor.submit(searcher);
//
//            f.addListener(new GenericFutureListener<Future<List<String>>>() {
//                @Override
//                public void operationComplete(Future<List<String>> matchingLines) throws Exception {
//                    StringBuilder builder = new StringBuilder();
//                    for (String s : matchingLines.get()) {
//                        builder.append(s);
//                        builder.append("<br>");
//                    }
//                    ByteBuf responseText = Unpooled.copiedBuffer(builder.toString(), CharsetUtil.UTF_8);
//                    DefaultFullHttpResponse response = new DefaultFullHttpResponse(
//                            HttpVersion.HTTP_1_1,
//                            HttpResponseStatus.OK,
//                            responseText);
//                    response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
//                    response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
//                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
//                }
//            });
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("ERROR ERROR");
        cause.printStackTrace();
        ctx.close();
    }




}
