package com.client.models.BackendManagement;

import communication.Request;
import communication.Response;
import utils.ConnectionInfo;
import utils.RequestCodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public enum BackendManager {
    INSTANCE;

    public static final int MAX_RETRY = 5;
    private int retryAttempts = 0;

    private final BackendEventReceiver eventHandler = new BackendEventReceiver();
    private Thread eventThread;
    private Socket socketConnection;

    private BufferedReader in;
    private PrintWriter out;

    public void CreateConnection() {
        try {

            //Clear current thread
            ClearThread();

            //Check and establish connection
            if (!Connect()) {
                ConnectionFailed();
                return;
            }

            //UI CALL (Close connection attempting if open)

            //Create streams
            in = new BufferedReader(new InputStreamReader(socketConnection.getInputStream()));
            out = new PrintWriter(socketConnection.getOutputStream(), true);

            //Create eventHandler thread
            eventThread = new Thread(eventHandler);
            eventThread.start();

        } catch (IOException e) {
            System.err.println("Fatal Error : IOException while connecting to back-end");
            e.printStackTrace();
            ConnectionFailed();
        }
    }

    private void ClearThread() {
        if (eventThread != null) {
            eventThread.interrupt();
            eventThread = null;
        }
    }

    private boolean Connect() {
        //Already connected
        if (socketConnection != null && socketConnection.isConnected()) {
            return true;
        }

        //Try connecting
        boolean connected = false;
        while (!connected && retryAttempts < MAX_RETRY) {
            connected = TryConnecting();
            retryAttempts++;
        }

        //Reset attempts and return
        retryAttempts = 0;
        return connected;
    }

    private boolean TryConnecting() {
        try {
            socketConnection = new Socket(ConnectionInfo.SERVER_IP, ConnectionInfo.SERVER_PORT);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public synchronized void ConnectionFailed() {
        //UI CALL (Show Connection Failed - Close or Retry)
    }

    public void ConnectionDropped() {
        //UI CALL (Show Connection attempting)
        CreateConnection();
    }

    public Socket getConnection() {
        return socketConnection;
    }

    public BufferedReader getIn() {
        return in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void SubmitRequest(RequestCodes code, Object payload, CompletableFuture<Response<Object>> callback) {
        new Thread(() -> {
            String id = UUID.randomUUID().toString();
            Request<Object> request = new Request<>(id, payload, code);

            //Turn it into JSON String or Send it as is (????)

            eventHandler.AddPendingRequest(id, callback);
            //out.println(""); //Print the request to the buffer
        }).start();

    }

}