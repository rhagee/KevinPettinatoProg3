package com.server.models.threads;

import communication.Response;
import utils.ConnectionInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class SocketHandler extends Thread {

    private static final Logger LOGGER = Logger.getLogger("SocketHandler");
    private static ConcurrentMap<String, MailHandlerSocket> addressToSocket = new ConcurrentHashMap<>();

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(ConnectionInfo.SERVER_PORT);
            LOGGER.info("Server listening on port " + ConnectionInfo.SERVER_PORT);
            while (!Thread.currentThread().isInterrupted()) {
                Socket connection = serverSocket.accept();
                String address = connection.getInetAddress().toString();
                LOGGER.info("Server listener creating connection for address" + address);
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
                MailHandlerSocket handler = new MailHandlerSocket(connection, in, out);
                addressToSocket.put(address, handler);
                Thread mailThread = new Thread(handler);
                mailThread.start();
            }
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.warning("Server stopped listening due to IOException.");
        }
    }

    public static MailHandlerSocket getMailHandler(String address) {
        return addressToSocket.get(address);
    }


    @Override
    public void interrupt() {
        LOGGER.info("Clearing sub-threads and interrupting.");
        //KillAll Sub-Threads
        addressToSocket.forEach((key, value) -> {
            value.interrupt();
        });

        //ClearMaps
        addressToSocket.clear();
        super.interrupt();
    }

}
