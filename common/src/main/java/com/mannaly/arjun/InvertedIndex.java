package com.mannaly.arjun;

import com.jcraft.jzlib.Adler32;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.CRC32;

public enum InvertedIndex {

    INSTANCE;

    private final static Logger logger = LoggerFactory.getLogger(FilePatternSearcher.class);

    private final List<DeflateCompressedContainer> compressedLines = new ArrayList<>();
    private final Map<String, List<Integer>> wordToContainingLinesMap = new HashMap<>();
    private final Pattern pattern = Pattern.compile("\\w+");

    private final Deflater deflater = new Deflater(9, true);

    // slowest compression but best compression.
    // http://stackoverflow.com/questions/9050260/what-does-a-zlib-header-look-like
    private static final byte[] adlerHeader = {0x78, (byte)0xDA};

    private static final byte[] eofTokenCompressed;
    private static final byte[] newLineTokenCompressed;
    private static final long newLineTokenAdler32;

    static {
        byte[] input = "<br>".getBytes(Charset.forName("UTF-8"));
        byte[] eof = new byte[6];
        byte[] newLine = new byte[10];

        Deflater deflater = new Deflater(9, true);

        deflater.setInput(input);
        deflater.finish();
        deflater.deflate(eof, 0, eof.length);
        deflater.reset();

        deflater.setInput(input);
        deflater.deflate(newLine, 0, newLine.length, Deflater.SYNC_FLUSH);
        deflater.reset();

        eofTokenCompressed = eof;
        newLineTokenCompressed = newLine;

        Adler32 adler32 = new Adler32();
        adler32.update(input, 0, input.length);
        newLineTokenAdler32 = adler32.getValue();
    }


    public void buildCache() {
        logger.info("Building inverted index...");
        long start = System.currentTimeMillis();

        InputStream resourceAsStream = FilePatternSearcher.class.getClassLoader().getResourceAsStream("war-and-peace.txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resourceAsStream));

            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                compressedLines.add(compressString(line));

                List<String> tokens = tokenize(line);
                for (String t : tokens) {
                    List<Integer> indexes = wordToContainingLinesMap.get(t);
                    if (indexes == null) {
                        indexes = new ArrayList<>();
                        wordToContainingLinesMap.put(t, indexes);
                    }
                    indexes.add(lineCount);
                }

                lineCount++;
            }

            long end = System.currentTimeMillis();
            logger.info("Build index in {} ms", end - start);
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
    }

    public ByteBuf find(String word) {
        List<Integer> linesWithWord = wordToContainingLinesMap.get(word);

        if (linesWithWord == null) {
            return Unpooled.copiedBuffer(" ".getBytes(Charset.forName("UTF-8")));
        }

        //TODO: need to set proper size for this?
        ByteBuf result = Unpooled.buffer();

        //write Zlib header.
        result.writeBytes(adlerHeader);

        long adler32 = 0;
        boolean isFirst = true;

        for (int i : linesWithWord) {
            DeflateCompressedContainer container = compressedLines.get(i);

            result.writeBytes(container.compressed, 0, container.bytesWritten);
            //result.writeBytes(newLineTokenCompressed, 0, newLineTokenCompressed.length);

            if (isFirst) {
                isFirst = false;
                adler32 = container.adler32;
            }
            else {
                adler32 = JZlib.adler32_combine(adler32, container.adler32, container.originalLength);
            }

            // checksum for <br>
            //adler32 = JZlib.adler32_combine(adler32, newLineTokenAdler32, 4);
        }

        //this terminates the gzip file.
        result.writeBytes(eofTokenCompressed, 0, eofTokenCompressed.length);

        adler32 = JZlib.adler32_combine(adler32, newLineTokenAdler32, 4);

        writeAdlerFooter(result, (int) adler32);

        return result;
    }

    private DeflateCompressedContainer compressString(String line) {
        byte[] input = line.getBytes(Charset.forName("UTF-8"));
        byte[] out = new byte[getUpperBoundForZlibOutput(input.length)];

        deflater.setInput(input);

        // we deflate with SYNC_FLUSH as we have to combine many of these raw deflate block together
        //TODO: don't think SYNC_FLUSH is useful here as we are doing a reset right after. Behavior is similar to FULL_FLUSH
        int bytesWritten = deflater.deflate(out, 0, out.length, Deflater.SYNC_FLUSH);
        deflater.reset();

        CRC32 crc32 = new CRC32();
        crc32.update(input, 0, input.length);
        return new DeflateCompressedContainer(out, input.length, bytesWritten, computeAdler32(input));
    }

    private int getUpperBoundForZlibOutput(int inputLength) {
        // ripped out straight from zlib source code.
        // https://github.com/madler/zlib/blob/50893291621658f355bc5b4d450a8d06a563053d/deflate.c#L566
        // we use raw deflate without any wrapper which gives this number.
        return inputLength + ((inputLength + 7) >> 3) + ((inputLength + 63) >> 6) + 5;
    }


    // adler32 checksum in big-endian.
    // https://tools.ietf.org/html/rfc1950#page-4
    private void writeAdlerFooter(ByteBuf buf, int adler32) {
        buf.writeByte(adler32 >>> 24);
        buf.writeByte(adler32 >>> 16);
        buf.writeByte(adler32 >>> 8);
        buf.writeByte(adler32);
    }

    private long computeAdler32(byte[] input) {
        Adler32 crc32 = new Adler32();
        crc32.update(input, 0, input.length);
        return crc32.getValue();
    }

    private List<String> tokenize(String line) {
        Matcher matcher = pattern.matcher(line);

        List<String> words = new ArrayList<>();
        while (matcher.find()) {
            words.add(matcher.group().toLowerCase());
        }
        return words;
    }

    private class DeflateCompressedContainer {
        byte[] compressed;
        int originalLength;
        int bytesWritten;
        long adler32;

        public DeflateCompressedContainer(byte[] compressed, int originalLenth, int bytesWritten, long adler32) {
            this.compressed = compressed;
            this.originalLength = originalLenth;
            this.bytesWritten = bytesWritten;
            this.adler32 = adler32;
        }
    }
}
