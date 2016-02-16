package com.mannaly.arjun;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ClientConnectionListener {

    private final ThreadPoolExecutor threadPool;

    private final ServerSocket socket;

    public ClientConnectionListener(int port) throws IOException {
        threadPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(100);
        threadPool.allowCoreThreadTimeOut(false);
        socket = new ServerSocket(port, 5000);
    }

    public void listen() {
        while (true) {
            try {
                Socket clientSocket = socket.accept();
                clientSocket.setKeepAlive(false);
                clientSocket.setSoTimeout(5000);
                threadPool.submit(new SyncRequestHandler(clientSocket));
                //System.out.printf("queue size - %d. pool size - %d. active count - %d \n", threadPool.getQueue().size(), threadPool.getPoolSize(), threadPool.getActiveCount());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}