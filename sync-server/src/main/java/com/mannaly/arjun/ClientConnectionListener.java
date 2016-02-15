package com.mannaly.arjun;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ClientConnectionListener {

    ThreadPoolExecutor threadPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(1000);
    private int connectionCount = 0;

    private final ServerSocket socket;

    public ClientConnectionListener(int port) throws IOException {
        threadPool.allowCoreThreadTimeOut(false);
        socket = new ServerSocket(port, 5000);
        //socket.setSoTimeout(0);
    }

    public void listen() {
        while (true) {
            try {
                Socket clientSocket = socket.accept();
                connectionCount++;
                //System.out.println(connectionCount);
                clientSocket.setKeepAlive(false);
                clientSocket.setSoTimeout(5000);
                threadPool.submit(new RequestHandler(clientSocket));
                //System.out.printf("queue size - %d. pool size - %d. active count - %d \n", threadPool.getQueue().size(), threadPool.getPoolSize(), threadPool.getActiveCount());
                //System.out.println(threadPool.);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}