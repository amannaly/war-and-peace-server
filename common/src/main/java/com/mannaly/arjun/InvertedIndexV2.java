package com.mannaly.arjun;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

public enum InvertedIndexV2 {

    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(InvertedIndexV2.class);

    private static final Map<String, ByteBuf> wordToCompressedLinesMap = new HashMap<>();

    private static final ByteBuf NO_RESULT = Unpooled.copiedBuffer(" ".getBytes(Charset.forName("UTF-8")));

    private static final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);

    public ByteBuf find(String word) {
        ByteBuf result = wordToCompressedLinesMap.get(word);

        if (result == null) {
            return NO_RESULT;
        }

        return result;
    }

    public void buildCache() {
        logger.info("Building cache ...");
        long start = System.currentTimeMillis();

        Map<String, StringBuilder> tempCache = readFile();;

        int compressedBytesLength;
        byte[] toCompress;

        for (Map.Entry<String, StringBuilder> e : tempCache.entrySet()) {
            toCompress = e.getValue().toString().getBytes(Charset.forName("UTF-8"));

            byte[] compressed = new byte[ZlibUtils.getUpperBoundForZlibOutput(toCompress.length)];
            deflater.setInput(toCompress);
            deflater.finish();
            compressedBytesLength = deflater.deflate(compressed);
            deflater.reset();

            // we use an unreleasableBuffer, otherwise the reference count will be decremented and hence 0 after writing to the wire.
            wordToCompressedLinesMap.put(e.getKey(), Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(compressed, 0, compressedBytesLength)));
        }

        long end = System.currentTimeMillis();
        logger.info("Built cache in {} ms", end - start);
    }

    private Map<String, StringBuilder> readFile() {
        Map<String, StringBuilder> tempCache = new HashMap<>();

        InputStream resourceAsStream = InvertedIndexV2.class.getClassLoader().getResourceAsStream("war-and-peace.txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resourceAsStream));

            String line;
            while ((line = reader.readLine()) != null) {

                List<String> tokens = StringUtils.tokenize(line);

                for (String t : tokens) {
                    StringBuilder builder = tempCache.get(t);
                    if (builder == null) {
                        builder = new StringBuilder();
                        tempCache.put(t, builder);
                        builder.append(line);
                    }
                    else {
                        builder.append("<br>");
                        builder.append(line);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return tempCache;
    }
}
