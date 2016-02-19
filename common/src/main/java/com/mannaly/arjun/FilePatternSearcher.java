package com.mannaly.arjun;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePatternSearcher {

    public static List<String> search(String query) throws IOException {
        long start = System.currentTimeMillis();
        BufferedReader reader = null;
        List<String> matchedLines = new ArrayList<>();

        try {
            InputStream resourceAsStream = FilePatternSearcher.class.getClassLoader().getResourceAsStream("war-and-peace.txt");
            reader = new BufferedReader(new InputStreamReader(resourceAsStream));
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
