package com.mannaly.arjun;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyncRequestHandler implements Runnable {

    private Socket clientSocket;

    private static final Pattern pattern = Pattern.compile("GET /\\?q=(.*) HTTP/1.1");

    public SyncRequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        BufferedReader reader;
        PrintWriter writer = null;

        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String line, query;
            List<String> matchedLines = new ArrayList<>(0);

            if ((line = reader.readLine()) != null && !line.equals("")) {
                query = getQuery(line);
                if (query != null) {
                    matchedLines = FilePatternSearcher.search(query);
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
}
