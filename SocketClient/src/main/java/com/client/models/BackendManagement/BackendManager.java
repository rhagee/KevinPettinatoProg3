package com.client.models.BackendManagement;

import communication.Request;
import communication.Response;
import utils.ConnectionInfo;
import utils.RequestCodes;

import java.io.*;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public enum BackendManager implements Runnable {
    INSTANCE;

    public static final int MAX_RETRY = 5;
    private int retryAttempts = 0;

    private final BackendEventReceiver eventHandler = new BackendEventReceiver();
    private Thread eventThread;
    private Socket socketConnection;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private ExecutorService writerExecutor;

    @Override
    public void run() {
        CreateConnection();
    }

    public synchronized void CreateConnection() {
        try {
            if (isConnectionSafe()) {
                return;
            }

            //Check and establish connection
            if (!Connect()) {
                ConnectionFailed();
                return;
            }

            //Create streams
            out = new ObjectOutputStream(socketConnection.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socketConnection.getInputStream());

            writerExecutor = Executors.newSingleThreadExecutor();
            //Create eventHandler thread
            eventThread = new Thread(eventHandler);
            eventThread.start();

            //UI CALL (Close Retry Dialog)
        } catch (IOException | InterruptedException e) {
            System.err.println("Fatal Error : Exception while connecting to back-end");
            e.printStackTrace();
            ConnectionFailed();
        }
    }

    private boolean Connect() throws InterruptedException {
        //Already connected
        if (socketConnection != null && socketConnection.isConnected()) {
            return true;
        }

        //Try connecting
        boolean connected = false;
        while (!connected && retryAttempts < MAX_RETRY) {
            connected = TryConnecting();
            retryAttempts++;
            if (!connected) {
                Thread.sleep(1000L * retryAttempts); //Exponential backoff
            }
        }

        //Reset attempts and return
        retryAttempts = 0;
        return connected;
    }

    private boolean TryConnecting() {
        try {
            socketConnection = new Socket(ConnectionInfo.SERVER_IP, ConnectionInfo.SERVER_PORT);
            socketConnection.setKeepAlive(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public synchronized void ClearConnection() {
        try {
            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }

            if (socketConnection != null) {
                socketConnection.close();
            }

            if (eventThread != null) {
                //If anything was listening fail them gracefully with IOException
                eventHandler.failAllPending(new IOException("Connection dropped"));
                eventThread.interrupt();
                eventThread = null;
            }

            writerExecutor.shutdown();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    //This is when we fail connecting multiple times (5)
    public synchronized void ConnectionFailed() {
        //UI CALL (Show Connection Failed - Close or Retry)
    }

    //If connection drops we clear the connection to ensure a new one will be established
    //Then call UI to show the attempts
    //Start create connection process
    public synchronized void ConnectionDropped() {
        ClearConnection();
        //UI CALL (Show Connection attempting)
        CreateConnection();
    }

    public Socket getConnection() {
        return socketConnection;
    }

    public ObjectInputStream getIn() {
        return in;
    }

    public ObjectOutputStream getOut() {
        return out;
    }

    public boolean isConnectionSafe() {
        return socketConnection != null && socketConnection.isConnected() && in != null && out != null && eventThread != null && eventThread.isAlive();
    }

    //Use the writerExecutor thread so we recycle one thread instead of instancing multiple
    public void trySubmitRequest(RequestCodes code, Object payload, CompletableFuture<Response<?>> callback) {
        if (writerExecutor == null) {
            return;
        }

        writerExecutor.submit(() -> {
            try {
                if (!isConnectionSafe()) {
                    ConnectionDropped();
                    return;
                }

                String id = UUID.randomUUID().toString();
                Request<Object> request = new Request<>(id, payload, code);
                eventHandler.AddPendingRequest(id, callback);
                out.writeObject(request);
                out.flush();
            } catch (IOException e) {
                System.err.println("Fatal Error : IOException while sending request to back-end");
                e.printStackTrace();
            }
        });
    }
}