package com.client.models.BackendManagement;

import com.client.models.AlertManagement.AlertManager;
import com.client.models.AlertManagement.AlertType;
import com.client.models.ProgApplication;
import communication.Request;
import communication.Response;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import utils.ConnectionInfo;
import utils.RequestCodes;
import utils.ResponseCodes;

import java.io.*;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public enum BackendManager implements Runnable {
    INSTANCE;

    public static final int MAX_RETRY = 3;
    public static final long RETRY_BACKOFF_MULTIPLIER = 1000L;

    private int retryAttempts = 0;
    private ObjectProperty<ConnectionState> state = new SimpleObjectProperty<>(ConnectionState.CONNECTING);

    private final BackendEventReceiver eventHandler = new BackendEventReceiver();
    private Thread eventThread;
    private Socket socketConnection;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private ExecutorService writerExecutor;

    public ObjectProperty<ConnectionState> getState() {
        return state;
    }

    private String token = null;

    @Override
    public void run() {
        CreateConnection();
    }

    public synchronized void CreateConnection() {
        System.out.println("Creating connection...");
        try {
            if (isConnectionSafe()) {
                ConnectionSuccess();
                return;
            }

            ClearConnection();
            //Check and establish connection
            if (!Connect()) {
                ConnectionFailed();
                return;
            }

            ConnectionSuccess();
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
            if (ProgApplication.APPLICATION_CLOSED) {
                System.out.println("BackendManager connection closed by Closing Application clean-up");
                return;
            }
            System.out.println("Server closed the connection");
            ConnectionFailed();
        } catch (InterruptedException e) {
            if (ProgApplication.APPLICATION_CLOSED) {
                return;
            }
            ConnectionFailed();
            System.out.println("BackendManager connection attempts interrupted while in sleep");
            e.printStackTrace();
        }
    }

    private boolean Connect() throws InterruptedException {
        //Already connected
        if (socketConnection != null && !socketConnection.isClosed()) {
            return true;
        }

        boolean connected = false;
        connected = TryConnecting();
        if (!connected) {
            System.out.println("Failed connecting to the server - RETRY LOOP");
            state.setValue(ConnectionState.CONNECTING);
            while (!connected && retryAttempts <= MAX_RETRY) {
                retryAttempts = retryAttempts + 1;
                connected = TryConnecting();
                if (!connected && retryAttempts <= MAX_RETRY) {
                    Thread.sleep(RETRY_BACKOFF_MULTIPLIER * retryAttempts); //Exponential backoff
                }
            }
        }
        System.out.println("Connection " + (connected ? "Success" : "Failed") + " After " + retryAttempts + " attempts");

        //Reset attempts and return
        retryAttempts = 0;

        return connected;
    }

    private void ConnectionSuccess() {
        state.setValue(ConnectionState.CONNECTED);
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


            if (socketConnection != null && !socketConnection.isClosed()) {
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
        state.setValue(ConnectionState.DISCONNECTED);
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
        return socketConnection != null && socketConnection.isConnected() && !socketConnection.isClosed() && in != null && out != null && eventThread != null && eventThread.isAlive();
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void clearToken() {
        this.token = null;
    }

    //Use the writerExecutor thread so we recycle one thread instead of instancing multiple
    public void trySubmitRequest(RequestCodes code, Object payload, CompletableFuture<Response<?>> callback) {
        if (writerExecutor == null || writerExecutor.isShutdown()) {
            CompleteWithError(callback);
            return;
        }

        writerExecutor.submit(() -> {
            try {
                if (!isConnectionSafe()) {
                    ConnectionDropped();
                    CompleteWithError(callback);
                    return;
                }

                String id = UUID.randomUUID().toString();
                Request<Object> request = new Request<>(id, payload, code, token);
                eventHandler.AddPendingRequest(id, callback);
                out.writeObject(request);
                out.flush();
            } catch (IOException e) {
                System.err.println("Fatal Error : IOException while sending request to back-end");
                e.printStackTrace();
                CompleteWithError(callback);
            }
        });
    }


    private void CompleteWithError(CompletableFuture<Response<?>> callback) {
        Response<?> response = new Response<String>(null, null, ResponseCodes.DISCONNECTED);
        callback.complete(response);
        AlertManager.get().add("Errore di connessione", "Impossibile collegarsi al server. Riconnettersi e riprovare.", AlertType.ERROR);
    }
}