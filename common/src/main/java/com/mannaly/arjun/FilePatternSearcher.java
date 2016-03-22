package com.mannaly.arjun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePatternSearcher {

    private final static Logger logger = LoggerFactory.getLogger(FilePatternSearcher.class);

    private static final List<String> FILE_DATA = new ArrayList<>();

    static {
        long start = System.currentTimeMillis();
        InputStream resourceAsStream = FilePatternSearcher.class.getClassLoader().getResourceAsStream("war-and-peace.txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resourceAsStream));
            String line;
            while ((line = reader.readLine()) != null) {
                FILE_DATA.add(line);
            }
            long end = System.currentTimeMillis();
            logger.debug("Read file from disk in {} ms", end-start);
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

    public static List<String> search(String query) throws IOException {
        long start = System.currentTimeMillis();
        List<String> matchedLines = new ArrayList<>();

        Pattern pattern = Pattern.compile(".*" + query + ".*", Pattern.CASE_INSENSITIVE);

        Matcher m = pattern.matcher("");
        for (String line : FILE_DATA) {
            m.reset(line);
            if (m.matches()) {
                matchedLines.add(line);
            }
        }

        long end = System.currentTimeMillis();
        //logger.debug("Searched file contents in {} ms.", end - start);
        return matchedLines;
    }
}
