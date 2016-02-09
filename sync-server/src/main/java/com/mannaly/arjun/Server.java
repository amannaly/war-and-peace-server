package com.mannaly.arjun;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void startServer() throws IOException {
        ClientConnectionListener listener = new ClientConnectionListener(8888);
        listener.listen();
    }

    public static void main(String[] args) {
        System.out.println("Starting sync server ...");

        try {
            startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
