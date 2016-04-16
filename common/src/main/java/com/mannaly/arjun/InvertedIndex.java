package com.mannaly.arjun;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum InvertedIndex {

    INSTANCE;

    private final static Logger logger = LoggerFactory.getLogger(FilePatternSearcher.class);

    private final List<ByteBuf> fileLines = new ArrayList<>();

    //TODO: try GNU trove for better performance.
    private final Map<String, List<Integer>> index = new HashMap<>();
    Pattern pattern = Pattern.compile("\\w+");

    public void buildCache() {
        logger.info("Building inverted index...");
        long start = System.currentTimeMillis();

        InputStream resourceAsStream = FilePatternSearcher.class.getClassLoader().getResourceAsStream("war-and-peace.txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resourceAsStream));
            String line;
            int lineCount = 0;
            ByteBuf lineBuf;
            while ((line = reader.readLine()) != null) {
                lineBuf = Unpooled.copiedBuffer(line, CharsetUtil.UTF_8);

                // Since we write the buffer directly to the wire it will be released
                // causing an io.netty.util.IllegalReferenceCountException when accessed a second time.
                // To prevent this we use unreleasableBuffer.
                fileLines.add(Unpooled.unreleasableBuffer(lineBuf));

                List<String> tokens = tokenize(line);
                for (String t : tokens) {
                    List<Integer> indexes = index.get(t);
                    if (indexes == null) {
                        indexes = new ArrayList<>();
                        index.put(t, indexes);
                    }
                    indexes.add(lineCount);
                }
                lineCount++;
            }
            long end = System.currentTimeMillis();
            logger.debug("Build index in {} ms", end-start);
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

    public List<ByteBuf> find(String word) {
        List<Integer> indexes = index.get(word);
        List<ByteBuf> lines = new ArrayList<>();

        if (indexes != null) {
            indexes.forEach(i -> lines.add(fileLines.get(i)));
        }
        return lines;
    }

    private List<String> tokenize(String line) {
        Matcher matcher = pattern.matcher(line);

        List<String> words = new ArrayList<>();
        while (matcher.find()) {
            words.add(matcher.group().toLowerCase());
        }
        return words;
    }
}
