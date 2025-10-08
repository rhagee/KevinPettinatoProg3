package com.server.models.threads;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.server.models.DatabaseHandler;
import communication.Request;
import communication.Response;
import utils.RequestCodes;
import utils.ResponseCodes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MailHandlerSocket extends Thread {

    private static final Logger LOGGER = Logger.getLogger("SocketWorker");

    private UUID id;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket client;

    private String mail;
    private Object requestObj;


    public MailHandlerSocket(UUID id, Socket client, ObjectInputStream in, ObjectOutputStream out) {
        this.id = id;
        this.client = client;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {

        LOGGER.info("Listening Requests from client: " + client.getRemoteSocketAddress());
        while (!Thread.currentThread().isInterrupted() && TryReadObject()) {

            //Exception
            if (requestObj == null) {
                continue;
            }

            //Wrong Type Check + Assign request with Cast
            if (!(requestObj instanceof Request<?> request)) {
                LOGGER.log(Level.WARNING, "Can't parse requestedObject", requestObj);
                continue;
            }

            HandleRequest(request);
        }
    }

    private void HandleRequest(Request<?> request) {
        RequestCodes code = request.getCode();
        switch (code) {
            case RequestCodes.AUTH:
                OnAuth(request);
                break;
            case RequestCodes.RECEIVE:
                OnReceive(request);
                break;
            case RequestCodes.SEND:
                OnSend(request);
                break;
            case RequestCodes.POLLING:
                OnPoll(request);
                break;
            case RequestCodes.DELETE:
                OnDelete(request);
                break;
            default:
                OnNotFound(request);
                break;
        }
    }

    private void OnNotFound(Request<?> request) {
        LOGGER.log(Level.WARNING, "Can't handle request with code", request.getCode().toString());
        SendObject(new Response<>(request.getRequestID(), "", ResponseCodes.NOTFOUND));
    }

    private void OnAuth(Request<?> request) {
        if (!TypeCheck(request, String.class)) {
            return;
        }

        String mail = (String) request.getPayload();
        String token = DatabaseHandler.INSTANCE.checkUser(mail);
        if (token == null) {
            LOGGER.log(Level.INFO, "Email : " + mail + " is not a valid account");
            SendObject(new Response<>(request.getRequestID(), "", ResponseCodes.UNAUTHORIZED));
            return;
        }

        this.mail = mail;
        SocketHandler.registerMailHandler(mail, this);
        SendObject(new Response<>(request.getRequestID(), token, ResponseCodes.OK));
    }

    private void OnReceive(Request<?> request) {
        if (!isAuthenticated(request)) {
            return;
        }
    }

    private void OnPoll(Request<?> request) {
        if (!isAuthenticated(request)) {
            return;
        }
    }

    private void OnSend(Request<?> request) {
        if (!isAuthenticated(request)) {
            return;
        }
    }

    private void OnDelete(Request<?> request) {
        if (!isAuthenticated(request)) {
            return;
        }
    }

    private boolean TypeCheck(Request<?> request, Class c) {

        if (!c.isInstance(request.getPayload())) {
            LOGGER.log(Level.WARNING, "Request " + request.getRequestID() + " has invalid payload type, expected " + c.toString() + " received " + request.getPayload().getClass());
            SendObject(new Response<>(request.getRequestID(), "", ResponseCodes.BADPARAM));
            return false;
        }

        return true;
    }

    private boolean isAuthenticated(Request<?> request) {
        try {
            String token = request.getToken();
            this.mail = DatabaseHandler.INSTANCE.DecodeToken(token); //Refresh Mail in case we'v lost it (re-connection)
            return true;
        } catch (JWTVerificationException exception) {
            LOGGER.log(Level.INFO, "Client " + client.getInetAddress().toString() + " not authorized to " + request.getCode().toString());
            SendObject(new Response<>(request.getRequestID(), "", ResponseCodes.UNAUTHORIZED));
            return false;
        }
    }

    private synchronized void SendObject(Response<?> res) {
        try {
            out.writeObject(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean TryReadObject() {
        try {
            return (requestObj = in.readObject()) != null;
        } catch (IOException e) {
            LOGGER.info("Connection with " + client.getRemoteSocketAddress() + " closed.");
            SocketHandler.removeHandler(id, mail);
            return false;
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Class not found", e);
            requestObj = null;
            return true;
        }
    }

    public void sendMail(Response<?> res) {
        SendObject(res);
    }

    @Override
    public void interrupt() {
        try {
            LOGGER.info("Interrupting connection with " + client.getRemoteSocketAddress());

            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException exception) {
            LOGGER.severe("Exception while interrupting thread");
        } finally {
            super.interrupt();
        }

    }

}
