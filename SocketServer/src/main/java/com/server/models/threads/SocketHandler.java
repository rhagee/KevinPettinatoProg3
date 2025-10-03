package com.server.models;

import utils.ConnectionInfo;

import java.net.ServerSocket;
import java.util.HashMap;

public class SocketHandler implements Runnable {

    public static HashMap<String, MailHandlerSocket> mailHandlerSocketHashMap = new HashMap<>();


    @Override
    public void run() {
        
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ServerSocket serverSocket = new ServerSocket(ConnectionInfo.SERVER_PORT);
            } catch (Exception e) {

            }
        }
    }

    public void stop() {

    }
}
