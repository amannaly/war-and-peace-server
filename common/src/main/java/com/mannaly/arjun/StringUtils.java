package com.mannaly.arjun;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern pattern = Pattern.compile("\\w+");

    public static List<String> tokenize(String line) {
        Matcher matcher = pattern.matcher(line);

        List<String> words = new ArrayList<>();
        while (matcher.find()) {
            words.add(matcher.group().toLowerCase());
        }
        return words;
    }
}
