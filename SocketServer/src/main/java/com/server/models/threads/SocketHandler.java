package com.server.models.threads;

import communication.Response;
import utils.ConnectionInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class SocketHandler extends Thread {

    private static final Logger LOGGER = Logger.getLogger("SocketHandler");
    private static ConcurrentMap<UUID, MailHandlerSocket> idToSocket = new ConcurrentHashMap<>();
    private static ConcurrentMap<String, List<MailHandlerSocket>> mailToSocketList = new ConcurrentHashMap<>();


    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(ConnectionInfo.SERVER_PORT);
            LOGGER.info("Server listening on port " + ConnectionInfo.SERVER_PORT);
            while (!Thread.currentThread().isInterrupted()) {
                Socket connection = serverSocket.accept();
                if (Thread.currentThread().isInterrupted()) {
                    connection.close();
                    return;
                }

                UUID id = UUID.randomUUID();
                LOGGER.info("Server listener creating connection with id : " + id);
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
                MailHandlerSocket handler = new MailHandlerSocket(id, connection, in, out);
                idToSocket.put(id, handler);
                Thread mailThread = new Thread(handler);
                mailThread.start();
            }
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.warning("Server stopped listening due to IOException.");
        }
    }

    public static MailHandlerSocket getMailHandler(String address) {
        return idToSocket.get(address);
    }


    public static void sendMail(String mail, Response<?> res) {
        mailToSocketList.get(mail).forEach((element) -> {
            element.sendMail(res);
        });
    }

    public static void registerMailHandler(String mail, MailHandlerSocket handler) {
        if (mail == null) {
            System.err.println("Dev flow error : Trying registering null mail");
            return;
        }

        if (mailToSocketList == null) {
            mailToSocketList = new ConcurrentHashMap<>();
        }

        if (!mailToSocketList.containsKey(mail)) {
            List<MailHandlerSocket> list = new ArrayList<>();
            list.add(handler);
            mailToSocketList.put(mail, list);
            return;
        }

        List<MailHandlerSocket> list = mailToSocketList.get(mail);
        list.add(handler);
    }

    public static void unregisterMailHandler(String mail, MailHandlerSocket handler) {
        if (mailToSocketList == null || mail == null || !mailToSocketList.containsKey(mail)) {
            return;
        }

        List<MailHandlerSocket> list = mailToSocketList.get(mail);
        list.remove(handler);
        LOGGER.info("Removed an handler for" + mail);
        if (list.isEmpty()) {
            mailToSocketList.remove(mail);
            LOGGER.info("No handlers left for mail : " + mail + ", removing from HashMap");
        }
    }

    public static void removeHandler(UUID id, String mail) {
        if (idToSocket == null || idToSocket.isEmpty()) {
            return;
        }

        MailHandlerSocket handler = idToSocket.remove(id);
        LOGGER.info("Removed " + id + " from hashmap");

        if (handler != null) {
            unregisterMailHandler(mail, handler);
        }


    }


    @Override
    public void interrupt() {
        System.out.println("Clearing sub-threads and interrupting.");
        //KillAll Sub-Threads
        idToSocket.forEach((key, value) -> {
            value.interrupt();
        });

        //ClearMaps
        mailToSocketList.clear();
        idToSocket.clear();
        super.interrupt();
    }

}
