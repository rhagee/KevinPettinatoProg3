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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SocketHandler extends Thread {

    private static ConcurrentMap<String, MailHandlerSocket> addressToSocket = new ConcurrentHashMap<>();
    private static ConcurrentMap<String, List<MailHandlerSocket>> mailToSocketList = new ConcurrentHashMap<>();

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(ConnectionInfo.SERVER_PORT);
            while (!Thread.currentThread().isInterrupted()) {
                Socket connection = serverSocket.accept();
                String address = connection.getInetAddress().toString();
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
                MailHandlerSocket handler = new MailHandlerSocket(connection, in, out);
                addressToSocket.put(address, handler);
                Thread mailThread = new Thread(handler);
                mailThread.start();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MailHandlerSocket getMailHandler(String address) {
        return addressToSocket.get(address);
    }


    public static void sendMail(String mail, Response<?> res) {
        mailToSocketList.get(mail).forEach((element) -> {
            element.sendMail(res);
        });
    }

    public static void registerMailHandler(String mail, MailHandlerSocket handler) {
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
        if (!mailToSocketList.containsKey(mail)) {
            return;
        }

        List<MailHandlerSocket> list = mailToSocketList.get(mail);
        list.remove(handler);
        if (list.isEmpty()) {
            mailToSocketList.remove(mail);
        }
    }


    @Override
    public void interrupt() {
        //KillAll Sub-Threads
        addressToSocket.forEach((key, value) -> {
            value.interrupt();
        });

        //ClearMaps
        addressToSocket.clear();
        mailToSocketList.clear();
        super.interrupt();
    }

}
