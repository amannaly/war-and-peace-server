package com.mannaly.arjun;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePatternSearcher {

    public static List<String> search(String query) throws IOException {
        long start = System.currentTimeMillis();
        BufferedReader reader = null;
        List<String> matchedLines;

        try {
            reader = new BufferedReader(new FileReader("/Users/amannaly/code/java-server/file1.txt"));
            matchedLines = new ArrayList<>();
            String line;
            Pattern pattern = Pattern.compile(".*" + query + ".*", Pattern.CASE_INSENSITIVE);
            Matcher m = pattern.matcher("");

            while ((line = reader.readLine()) != null) {
                m.reset(line);
                if (m.matches()) {
                    matchedLines.add(line);
                }
            }
        }
        finally {
             if(reader != null)
                 reader.close();
        }

        long end = System.currentTimeMillis();
        System.out.println(end-start);
        return matchedLines;
    }
}
