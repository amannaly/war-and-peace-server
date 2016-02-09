package com.mannaly.arjun;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientConnectionListener {

    ExecutorService threadPool = Executors.newFixedThreadPool(1000);
    private int connectionCount = 0;

    private final ServerSocket socket;

    public ClientConnectionListener(int port) throws IOException {
        socket = new ServerSocket(port, 3000);
    }

    public void listen() {
        while (true) {
            try {
                Socket clientSocket = socket.accept();
                connectionCount++;
                //System.out.println(connectionCount);
                clientSocket.setKeepAlive(true);
                clientSocket.setSoTimeout(0);
                threadPool.submit(new RequestHandler(clientSocket));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}