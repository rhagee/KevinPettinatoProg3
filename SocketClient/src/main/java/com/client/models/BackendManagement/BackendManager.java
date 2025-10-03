package com.client.models.BackendManagement;

import com.client.models.AlertManagement.AlertManager;
import communication.Request;
import communication.Response;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
    private IntegerProperty retryAttempts = new SimpleIntegerProperty(0);

    private final BackendEventReceiver eventHandler = new BackendEventReceiver();
    private Thread eventThread;
    private Socket socketConnection;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private ExecutorService writerExecutor;

    public IntegerProperty getRetryAttempts() {
        return retryAttempts;
    }

    @Override
    public void run() {
        CreateConnection();
    }

    public synchronized void CreateConnection() {
        System.out.println("Creating connection...");
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
        } catch (IOException e) {
            System.err.println("Fatal Error : Exception while connecting to back-end");
            e.printStackTrace();
            ConnectionFailed();
        } catch (InterruptedException e) {
            System.out.println("BackendManager connection attempts interrupted while in sleep");
        }
    }

    private boolean Connect() throws InterruptedException {
        //Already connected
        if (socketConnection != null && socketConnection.isConnected()) {
            return true;
        }

        boolean connected = false;
        connected = TryConnecting();
        if (!connected) {
            System.out.println("Failed connecting to the server - RETRY LOOP");
            AlertManager.get().OnConnectionDropped();

            while (!connected && retryAttempts.getValue() <= MAX_RETRY) {
                retryAttempts.setValue(retryAttempts.getValue() + 1);
                connected = TryConnecting();
                if (!connected && retryAttempts.getValue() <= MAX_RETRY) {
                    Thread.sleep(500L * retryAttempts.getValue()); //Exponential backoff
                }
            }
        }
        System.out.println("Connection " + (connected ? "Success" : "Failed") + " After " + retryAttempts.getValue().toString() + " attempts");

        //Reset attempts and return
        retryAttempts.setValue(0);

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
                System.out.println("Closing Input Stream");
                in.close();
            }

            if (out != null) {
                System.out.println("Closing Output Stream");
                out.close();
            }


            if (socketConnection != null) {
                System.out.println("Closing Socket");
                socketConnection.close();
            }


            if (eventThread != null) {

                System.out.println("Interrupting thread");
                //If anything was listening fail them gracefully with IOException
                eventHandler.failAllPending(new IOException("Connection dropped"));
                eventThread.interrupt();
                eventThread = null;
            }


            System.out.println("Shutdown writerExecutor");
            if (writerExecutor != null) {
                writerExecutor.shutdown();
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    //This is when we fail connecting multiple times (5)
    public synchronized void ConnectionFailed() {
        AlertManager.get().OnConnectionFailed();
    }

    //If connection drops we clear the connection to ensure a new one will be established
    //Then call UI to show the attempts
    //Start create connection process
    public synchronized void ConnectionDropped() {
        ClearConnection();
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