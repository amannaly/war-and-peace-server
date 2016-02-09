package com.mannaly.arjun;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestHandler implements Runnable {

    private Socket clientSocket;

    private static final Pattern pattern = Pattern.compile("GET /\\?q=(.*) HTTP/1.1");

    public RequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        PrintWriter writer = null;

        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String line, query;
            boolean isFirst = true;
            List<String> matchedLines = new ArrayList<>(0);

            while ((line =reader.readLine()) != null && !line.equals("")) {
                if(isFirst) {
                    query = getQuery(line);
                    if (query != null) {
                        //System.out.println("got query string -> " + query);
                        matchedLines = search(query);
                    }
                    isFirst = false;
                }
            }

            writer = new PrintWriter(clientSocket.getOutputStream());

            // headers
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: text/html");
            writer.println("HTTP/1.1 200 OK");
            writer.println("");

            //body.
            if (matchedLines.isEmpty()) {
                writer.println("query not present in text");
            }
            else {
                StringBuilder builder = new StringBuilder();
                for (String l : matchedLines) {
                    builder.append(l + "<br>");
                }
                writer.println(builder.toString());
            }

            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
            writer.println("HTTP/1.1 500 OK");
            writer.println("");
            writer.flush();
        } finally {
            try {
                if (clientSocket != null)
                    clientSocket.close();
//                if (reader != null)
//                    reader.close();
//                if (writer != null)
//                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getQuery(String request) {
        Matcher matcher = pattern.matcher(request);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        else
            return null;
    }

    private List<String> search(String query) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("file.txt"));
        List<String> matchedLines = new ArrayList<>();
        String line;
        int lineCount = 1;
        Pattern pattern = Pattern.compile(".*" + query + ".*", Pattern.CASE_INSENSITIVE);

        while ((line = reader.readLine()) != null) {
            if (pattern.matcher(line).matches()) {
                matchedLines.add("<b>" + lineCount++ + ": </b>" + line);
            }
        }
        return matchedLines;
    }
}
