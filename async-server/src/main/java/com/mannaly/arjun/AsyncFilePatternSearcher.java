package com.mannaly.arjun;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;

public class AsyncFilePatternSearcher {

    private static final String FILE = "/Users/amannaly/code/java-server/file.txt";

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(50);

    public static void search(String query, ChannelHandlerContext ctx) throws IOException {
        Path p = Paths.get(FILE);
        Set<StandardOpenOption> options = new HashSet<>();
        options.add(StandardOpenOption.READ);

        AsynchronousFileChannel f = AsynchronousFileChannel.open(p, options, threadPool);

        int fileSize = (int)f.size();
        ByteBuffer dataBuffer = ByteBuffer.allocate(fileSize);

        Result r = new Result();
        r.buffer = dataBuffer;
        r.matchedLines = new ArrayList<>();
        r.query = query;
        r.ctx = ctx;
        r.f = f;

        f.read(dataBuffer, 0, r, new ReadHandler());
    }

    static class Result {
        ByteBuffer buffer;
        List<String> matchedLines;
        String query;
        ChannelHandlerContext ctx;
        AsynchronousFileChannel f;
    }

    static class ReadHandler implements CompletionHandler<Integer, Result> {
        @Override
        public void completed(Integer r, Result result) {
            byte[] byteData = result.buffer.array();
            Charset cs = Charset.forName("UTF-8");
            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(byteData), "UTF-8"));
                String line;
                int lineCount = 1;
                Pattern pattern = Pattern.compile(".*" + result.query + ".*", Pattern.CASE_INSENSITIVE);

                while ((line = reader.readLine()) != null) {
                    if (pattern.matcher(line).matches()) {
                        result.matchedLines.add("<b>" + lineCount++ + ": </b>" + line);
                    }
                }

                StringBuilder builder = new StringBuilder();
                for (String s : result.matchedLines) {
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
                result.ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);;


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null)
                    try {
                        reader.close();
                        result.f.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }

        @Override
        public void failed(Throwable exc, Result attachment) {

        }
    }
}
