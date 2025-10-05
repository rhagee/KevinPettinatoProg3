package com.server.models.threads;

import com.server.models.DatabaseHandler;
import communication.Request;
import communication.Response;
import utils.RequestCodes;
import utils.ResponseCodes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MailHandlerSocket extends Thread {


    private static final Logger LOGGER = Logger.getLogger("MAIL_HANDLER");

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket client;

    private Object requestObj;

    private String mail;
    private boolean isLogged = false;

    public String getMail() {
        return mail;
    }


    public MailHandlerSocket(Socket client, ObjectInputStream in, ObjectOutputStream out) {
        this.client = client;
        this.in = in;
        this.out = out;
        isLogged = false;
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted() && TryReadObject()) {

            //Exception
            if (requestObj == null) {
                continue;
            }

            //Wrong Type
            if (!(requestObj instanceof Request<?>)) {
                LOGGER.log(Level.WARNING, "Can't parse requestedObject", requestObj);
                continue;
            }

            Request<?> request = (Request<?>) requestObj;
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
        if (!DatabaseHandler.INSTANCE.CheckUser(mail)) {
            LOGGER.log(Level.INFO, "Email : " + mail + " is not a valid account");
            SendObject(new Response<>(request.getRequestID(), "", ResponseCodes.UNAUTHORIZED));
            return;
        }

        this.mail = mail;
        SocketHandler.registerMailHandler(mail, this);
        SendObject(new Response<>(request.getRequestID(), "", ResponseCodes.OK));
    }

    private void OnReceive(Request<?> request) {
        if (!isAuthenticated(request)) {
            return;
        }
    }

    private void OnSend(Request<?> request) {
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
        if (isLogged && mail != null) {
            return true;
        }

        LOGGER.log(Level.INFO, "Client " + client.getInetAddress().toString() + " not authorized to " + request.getCode().toString());
        SendObject(new Response<>(request.getRequestID(), "", ResponseCodes.UNAUTHORIZED));
        return false;
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

            //Unregister if connection is closed
            if (isLogged && mail != null) {
                SocketHandler.unregisterMailHandler(mail, this);
            }

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

            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            super.interrupt();
        }

    }

}
